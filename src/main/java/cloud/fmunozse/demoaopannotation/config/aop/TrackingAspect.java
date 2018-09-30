package cloud.fmunozse.demoaopannotation.config.aop;

import cloud.fmunozse.demoaopannotation.config.annotations.Tracking;
import cloud.fmunozse.demoaopannotation.config.annotations.TrackingParam;
import cloud.fmunozse.demoaopannotation.repository.TrackingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class TrackingAspect {

    ObjectMapper objectMapper;
    TrackingRepository trackingRepository;

    public TrackingAspect(ObjectMapper objectMapper, TrackingRepository trackingRepository) {
        this.objectMapper = objectMapper;
        this.trackingRepository = trackingRepository;
    }

    @Around("@annotation(cloud.fmunozse.demoaopannotation.config.annotations.Tracking)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        //Validate correct use of annotation
        if (request == null)
            throw new RuntimeException(String.format("Not allow Tracking in NO Rest java class %s - %s",
                    joinPoint.getTarget().getClass().getName(), joinPoint.getSignature().getName()));


        //Preparation inputJSON
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Tracking annotation = method.getAnnotation(Tracking.class);
        Map inputObject = getInputJson(method, joinPoint.getArgs());
        String inputObjectJSON = objectMapper.writeValueAsString(inputObject);


        //Call to the method
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;


        //Preparation outputJSON
        Object outputObject = (proceed instanceof ResponseEntity) ? ((ResponseEntity) proceed).getBody() : proceed;

        //Generate hash
        String hashKey = generateHashKey(annotation, inputObjectJSON, request);

        //Save record
        trackingRepository.save(cloud.fmunozse.demoaopannotation.domain.Tracking.builder()
                .method(request.getMethod())
                .urlRequest(getCompleteUrl(request))
                .hashRequest(hashKey)
                .inputJson(inputObjectJSON)
                .outputJson(outputObject != null ? objectMapper.writeValueAsString(outputObject) : null)
                .build());

        log.debug("Time Taken by {} is {}", joinPoint, executionTime);
        return proceed;

    }


    private String generateHashKey(Tracking annotation, String inputObjectJSON, HttpServletRequest request) {
        String inputJson = getKeysForInputJson(annotation, inputObjectJSON);
        String method = request.getMethod();
        String url = getCompleteUrl(request);

        log.info("Hash calculcation. InputJson: {}, method:{}, url:{}", inputJson, method, url);

        StringBuilder sb = new StringBuilder();
        sb.append(inputJson).append(method).append(url);

        return DigestUtils.md5DigestAsHex(sb.toString().getBytes());
    }

    private String getCompleteUrl(HttpServletRequest request) {
        return Arrays.stream(new String[]{request.getRequestURI(), request.getQueryString()})
                .filter(Objects::nonNull)
                .collect(Collectors.joining("?"));
    }


    private String getKeysForInputJson(Tracking annotation, String inputObjectJSON) {

        String[] hashKeys = annotation.hashKey();
        if (hashKeys == null || hashKeys.length == 0) {
            return inputObjectJSON;
        }

        return Arrays.stream(annotation.hashKey())
                .map(hashKey -> String.valueOf((Object) JsonPath.read(inputObjectJSON, hashKey)))
                .collect(Collectors.joining("#"));
    }


    private Map getInputJson(Method method, Object[] args) {

        //Map to keep the params that has been tagged with TrackingParam annotation
        Map keyParams = new LinkedHashMap();

        //Map to keep all paams (Used in case that is not used the TrackingParam)
        Map allParams = new LinkedHashMap();
        boolean containsTrackingParam = false;

        Annotation[][] annotationMatrix = method.getParameterAnnotations();
        for (int i = 0; i < args.length; i++) {
            Annotation[] annotations = annotationMatrix[i];
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == TrackingParam.class) {
                    keyParams.put(((TrackingParam) annotation).key(), args[i]);
                    containsTrackingParam = true;
                }
            }
            allParams.put(String.format("arg%s", i), args[i]);
        }
        return containsTrackingParam ? keyParams : allParams;
    }


    private Object getInputJsonTemporal(HttpServletRequest request, Parameter[] params) {
        Map keyParams = new HashMap();
        for (Parameter param : params) {
            try {
                Annotation keyAnno = param.getAnnotation(TrackingParam.class);
                if (keyAnno != null) {
                    keyParams.put(param.getName(), param.toString());
                }
            } catch (Exception e) {
                log.error("error ", e);
            }
        }

        return keyParams;
    }

    private Map<String, String> getHeadersInfo(HttpServletRequest request) {
        Map<String, String> map = new HashMap<String, String>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        return map;
    }
}

