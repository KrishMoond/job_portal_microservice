import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { of, tap, shareReplay } from 'rxjs';

interface CacheEntry {
  response: HttpResponse<any>;
  timestamp: number;
}

class HttpCacheService {
  private cache = new Map<string, CacheEntry>();
  private readonly TTL = 5 * 60 * 1000; // 5 minutes

  get(url: string): HttpResponse<any> | null {
    const entry = this.cache.get(url);
    if (!entry) return null;
    
    if (Date.now() - entry.timestamp > this.TTL) {
      this.cache.delete(url);
      return null;
    }
    
    return entry.response;
  }

  set(url: string, response: HttpResponse<any>): void {
    this.cache.set(url, { response, timestamp: Date.now() });
  }

  clear(): void {
    this.cache.clear();
  }
}

const cacheService = new HttpCacheService();

export const cacheInterceptor: HttpInterceptorFn = (req, next) => {
  // Only cache GET requests for static data
  if (req.method !== 'GET') {
    return next(req);
  }

  // ONLY cache companies and search results - everything else should be fresh
  const shouldCache = req.url.includes('/api/companies') || req.url.includes('/api/search/jobs');
  
  if (!shouldCache) {
    return next(req);
  }

  const cachedResponse = cacheService.get(req.url);
  if (cachedResponse) {
    return of(cachedResponse.clone());
  }

  return next(req).pipe(
    tap(event => {
      if (event instanceof HttpResponse) {
        cacheService.set(req.url, event);
      }
    })
  );
};
