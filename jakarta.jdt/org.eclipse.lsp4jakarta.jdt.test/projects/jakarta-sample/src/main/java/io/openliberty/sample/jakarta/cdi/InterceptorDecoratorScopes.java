package io.openliberty.sample.jakarta.cdi;

import jakarta.interceptor.Interceptor;
import jakarta.decorator.Decorator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Singleton;

public class InterceptorDecoratorScopes {
    
    // Invalid: @Interceptor with @ApplicationScoped
    @Interceptor
    @ApplicationScoped
    public static class InvalidInterceptorWithApplicationScoped {
    }
    
    // Invalid: @Decorator with @RequestScoped
    @Decorator
    @RequestScoped
    public static class InvalidDecoratorWithRequestScoped {
    }
    
    // Valid: @Interceptor with @Dependent
    @Interceptor
    @Dependent
    public static class ValidInterceptorWithDependent {
    }
    
    // Valid: @Decorator with @Dependent
    @Decorator
    @Dependent
    public static class ValidDecoratorWithDependent {
    }
    
    // Invalid: @Interceptor with @Dependent and @SessionScoped (has invalid scope)
    @Interceptor
    @Dependent
    @SessionScoped
    public static class InvalidInterceptorWithMultipleScopes {
    }
    
    // Valid: @Decorator with no scope annotation (defaults to @Dependent)
    @Decorator
    public static class ValidDecoratorWithNoScope {
    }
    
    // Valid: @Interceptor with no scope annotation (defaults to @Dependent)
    @Interceptor
    public static class ValidInterceptorWithNoScope {
    }
    
    // Invalid: @Decorator with @Singleton
    @Decorator
    @Singleton
    public static class InvalidDecoratorWithSingleton {
    }
}

// Made with Bob
