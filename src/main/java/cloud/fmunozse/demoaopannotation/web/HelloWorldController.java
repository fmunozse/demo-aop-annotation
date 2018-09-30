package cloud.fmunozse.demoaopannotation.web;


import cloud.fmunozse.demoaopannotation.config.annotations.Tracking;
import cloud.fmunozse.demoaopannotation.config.annotations.TrackingParam;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@Controller
public class HelloWorldController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Tracking
    @GetMapping("/hello-world-noTag")
    @ResponseBody
    public ResponseEntity<Greeting> sayHelloNoTag(
            @RequestParam(name = "name", required = false, defaultValue = "Stranger") String name,
            @RequestParam(name = "test1", required = false, defaultValue = "defaultValuetest1") String test1
    ) {
        return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, name)));
    }

    @Tracking
    @GetMapping("/hello-world-1tag")
    @ResponseBody
    public ResponseEntity<Greeting> sayHelloOneTag(
            @RequestParam(name = "name", required = false, defaultValue = "Stranger") @TrackingParam(key = "nameInputKey") String name,
            @RequestParam(name = "test1", required = false, defaultValue = "defaultValuetest1") String test1
    ) {
        return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, name)));
    }

    @Tracking (hashKey = {"$.test1"})
    @GetMapping("/hello-world-2tag")
    @ResponseBody
    public ResponseEntity<Greeting> sayHelloTwoTag(
            @RequestParam(name = "name", required = false, defaultValue = "Stranger") @TrackingParam(key = "nameInputKey") String name,
            @RequestParam(name = "test1", required = false, defaultValue = "defaultValuetest1") @TrackingParam(key = "test1") String test1) {
        return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, name)));
    }


    @Tracking(hashKey = {"$.greetingInputKey.id", "$.greetingInputKey.content"})
    @PostMapping("/helloworld")
    @ResponseBody
    public ResponseEntity<Greeting> sayHelloPost(
            @RequestBody @TrackingParam(key = "greetingInputKey") Greeting greeting) {
        return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, greeting.getContent())));
    }



}