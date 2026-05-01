import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { LucideAngularModule, Globe, BookmarkMinus, Search, MapPin, Briefcase, UserPlus, Loader2, Mail, Lock, Eye, EyeOff, User, ArrowLeft, ArrowRight, Bell, BellOff, Bookmark, Upload, LogOut, ChevronDown, ChevronRight, Clock, DollarSign, Building2, Calendar, FileText, FileX2, Send, Check, X, XCircle, AlertCircle, Info, Home, Settings, Filter, Plus, Edit, Pencil, Trash2, Download, Share2, Heart, Star, Gift, TrendingUp, Users, BarChart3, Target, CheckCircle2, HeartPulse, Sun, BookOpen, GraduationCap, Map, Banknote, SearchX, Zap, ExternalLink, MoreHorizontal, Paperclip, ShieldCheck, Menu, RefreshCw, Inbox } from 'lucide-angular';

import { routes } from './app.routes';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';

import { cacheInterceptor } from './core/interceptors/cache.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([jwtInterceptor, errorInterceptor, cacheInterceptor])
    ),
    importProvidersFrom(
      LucideAngularModule.pick({
        Globe,
        BookmarkMinus,
        Search,
        MapPin,
        Briefcase,
        UserPlus,
        Loader2,
        Mail,
        Lock,
        Eye,
        EyeOff,
        User,
        ArrowLeft,
        ArrowRight,
        Bell,
        BellOff,
        Bookmark,
        Upload,
        LogOut,
        ChevronDown,
        ChevronRight,
        Clock,
        DollarSign,
        Building2,
        Calendar,
        FileText,
        Send,
        Check,
        X,
        XCircle,
        AlertCircle,
        Info,
        Home,
        Settings,
        Filter,
        Plus,
        Edit,
        Trash2,
        Download,
        Share2,
        Heart,
        Star,
        Gift,
        TrendingUp,
        Users,
        BarChart3,
        Target,
        CheckCircle2,
        HeartPulse,
        Sun,
        BookOpen,
        GraduationCap,
        Map,
        Banknote,
        SearchX,
        Zap,
        ExternalLink,
        FileX2,
        Pencil,
        Paperclip,
        MoreHorizontal,
        ShieldCheck,
        Menu,
        RefreshCw,
        Inbox
      })
    )
  ]
};
