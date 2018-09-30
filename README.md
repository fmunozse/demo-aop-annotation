# demo-aop-annotation

Application to create a couple of annotations (at level of method and at level of param) to record call REST to a Controller.

See javadoc of Tracking annotation witch contains examples of use:

Annotation to keep the tracking of a REST method. This annotation keeps all request done in a table tracking (see cloud.fmunozse.demoaopannotation.domain.Tracking).
By default: Keep all input params in a JSON format (using the arg0, arg1 naming convention) and the output JSON format ( In case that return an EntityResponse, then, keeps the body like output). Also generate a hashing MD5 of
* Complete input JSON (this customized using the hashKey()
* url (+params)
* method (GET, POST, etc..)

Also it is possible to customized what is "included" like inputJson. In the param of method could be use the annotation see TrackingParam).
See next examples of use:

* By default:
```java
     @Tracking
     @GetMapping("/hello-world-noTag")
     @ResponseBody
     public ResponseEntity<Greeting> sayHelloNoTag(
             @RequestParam(name = "name", required = false, defaultValue = "Stranger") String name,
             @RequestParam(name = "test1", required = false, defaultValue = "defaultValuetest1") String test1
     ) {
         return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, name)));
     }
```
 
* Using TrackingParam to set the inputJson
 ```java
     @Tracking
     @GetMapping("/hello-world-1tag")
     @ResponseBody
     public ResponseEntity<Greeting> sayHelloOneTag(
             @RequestParam(name = "name", required = false, defaultValue = "Stranger") @TrackingParam(key = "nameInputKey") String name,
             @RequestParam(name = "test1", required = false, defaultValue = "defaultValuetest1") String test1
     ) {
         return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, name)));
     }
 ```
 
 
* Using TrackingParam to set the inputJson with 2 input params and use JsonPath to define what is the key for hash
```java 
     @Tracking (hashKey = { " $.test1 " })
     @GetMapping("/hello-world-2tag")
     @ResponseBody
     public ResponseEntity<Greeting> sayHelloTwoTag(
             @RequestParam(name = "name", required = false, defaultValue = "Stranger") @TrackingParam(key = "nameInputKey") String name,
             @RequestParam(name = "test1", required = false, defaultValue = "defaultValuetest1") @TrackingParam(key = "test1") String test1) {
         return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, name)));
     }
``` 

* Using for POST method
```java 
     @Tracking(hashKey = {"$.greetingInputKey.id", "$.greetingInputKey.content"})
     @PostMapping("/helloworld")
     @ResponseBody
     public ResponseEntity<Greeting> sayHelloPost(
             @RequestBody @TrackingParam(key = "greetingInputKey") Greeting greeting) {
         return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, greeting.getContent())));
     }
```

This is the result in the ddbb of the 4 examples

| id | createAt | hash | inputJson | method | outputJson | url |
| ---  | --- | --- | --- | --- | --- | --- |
|2	|2018-09-30 22:35:20.622	|86f5d8a4533f06781f2c5333d562a456	|{"arg0":"pepe","arg1":"defaultValuetest1"}	|GET	|{"id":2,"content":"Hello, pepe!"}	|/hello-world-noTag?name=pepe
|3	|2018-09-30 22:35:22.796	|1f30b88a33f60d7704f0dc331659b085	|{"nameInputKey":"pepe1"}	|GET	|{"id":3,"content":"Hello, pepe1!"}	|/hello-world-1tag?name=pepe1
|4	|2018-09-30 22:35:25.24	    |276167bc49ef83475fd0006f34f80646	|{"nameInputKey":"pepe1","test1":"defaultValuetest1"}	|GET	|{"id":4,"content":"Hello, pepe1!"}	|/hello-world-2tag?name=pepe1
|5	|2018-09-30 22:35:27.463	|2f957bf865844b53667d2e29f3d26561	|{"greetingInputKey":{"id":1,"content":"Hello, pepe!"}}	|POST	|{"id":5,"content":"Hello, Hello, pepe!!"}	|/helloworld

     