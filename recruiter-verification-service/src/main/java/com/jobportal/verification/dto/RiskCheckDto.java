package com.jobportal.verification.dto;

public record RiskCheckDto(String name, boolean passed, String summary, String expected, String actual) {}
