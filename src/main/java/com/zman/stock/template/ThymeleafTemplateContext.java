package com.zman.stock.template;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.VariablesMap;

/**
 * Themeleaf TemplateEngine需要用到。
 * 
 * @author zman
 *
 */
public class ThymeleafTemplateContext implements IWebContext {

    private VariablesMap<String, Object> variablesMap = new VariablesMap<>();

    public void setVariable(String key, Object value) {
        variablesMap.put(key, value);
    }

    @Override
    public VariablesMap<String, Object> getVariables() {
        return variablesMap;
    }

    @Override
    public Locale getLocale() {
        return new Locale("zh");
    }

    @Override
    public void addContextExecutionInfo(String templateName) {
        // TODO Auto-generated method stub

    }

    @Override
    public HttpServletRequest getHttpServletRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpServletResponse getHttpServletResponse() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpSession getHttpSession() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServletContext getServletContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VariablesMap<String, String[]> getRequestParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VariablesMap<String, Object> getRequestAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VariablesMap<String, Object> getSessionAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VariablesMap<String, Object> getApplicationAttributes() {
        // TODO Auto-generated method stub
        return null;
    }


}
