package com.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class MVC extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        // корневой контекст не нужен
        return null;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        // подключаем основную конфигурацию
        return new Class[] {SpringConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        // все запросы идут через /
        return new String[] {"/"};
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // тут вся инициализация
        super.onStartup(servletContext);
        addHiddenMethodFilter(servletContext);
    }

    private void addHiddenMethodFilter(ServletContext context) {
        // фильтр для поддержки скрытых методов (PUT, DELETE)
        context.addFilter("hiddenHttpMethodFilter", new HiddenHttpMethodFilter())
                .addMappingForUrlPatterns(null, true, "/*");
    }
}
