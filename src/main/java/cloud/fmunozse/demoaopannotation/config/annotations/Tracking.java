package cloud.fmunozse.demoaopannotation.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation to keep the tracking of a REST method. This annotation keeps all request done in a table tracking
 * (see {@link cloud.fmunozse.demoaopannotation.domain.Tracking }).</p>
 *
 * <p>By default: Keep all input params in a JSON format (using the arg0, arg1 naming convention) and the output JSON format
 *    ( In case that return an EntityResponse, then, keeps the body like output). Also generate a hashing MD5 of
 *    <ul>
 *      <li>Complete input JSON (this customized using the {@link #hashKey()}</li>
 *      <li>url (+params)</li>
 *      <li>method (GET, POST, etc..) </li>
 *    </ul>
 * </p>
 *
 * <p>Also it is possible to customized what is "included" like inputJson. In the param of method could be use the annotation see
 *   {@link TrackingParam }).</p>
 *
 * <p>See next examples of use:</p>
 *
 * <p>By default:</p>
 * <pre>{@code
 *     @Tracking
 *     @GetMapping("/hello-world-noTag")
 *     @ResponseBody
 *     public ResponseEntity<Greeting> sayHelloNoTag(
 *             @RequestParam(name = "name", required = false, defaultValue = "Stranger") String name,
 *             @RequestParam(name = "test1", required = false, defaultValue = "defaultValuetest1") String test1
 *     ) {
 *         return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, name)));
 *     }
 * }</pre>
 *
 * <p>Using TrackingParam to set the inputJson</p>
 * <pre>{@code
 *     @Tracking
 *     @GetMapping("/hello-world-1tag")
 *     @ResponseBody
 *     public ResponseEntity<Greeting> sayHelloOneTag(
 *             @RequestParam(name = "name", required = false, defaultValue = "Stranger") @TrackingParam(key = "nameInputKey") String name,
 *             @RequestParam(name = "test1", required = false, defaultValue = "defaultValuetest1") String test1
 *     ) {
 *         return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, name)));
 *     }
 * }</pre>
 *
 * <p>Using TrackingParam to set the inputJson with 2 input params and use JsonPath to define what is the key for hash</p>
 * <pre>{@code
 *     @Tracking (hashKey = { " $.test1 " })
 *     @GetMapping("/hello-world-2tag")
 *     @ResponseBody
 *     public ResponseEntity<Greeting> sayHelloTwoTag(
 *             @RequestParam(name = "name", required = false, defaultValue = "Stranger") @TrackingParam(key = "nameInputKey") String name,
 *             @RequestParam(name = "test1", required = false, defaultValue = "defaultValuetest1") @TrackingParam(key = "test1") String test1) {
 *         return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, name)));
 *     }
 * }</pre>
 *
 * <p>Using for POST method</p>
 * <pre>{@code
 *     @Tracking(hashKey = {"$.greetingInputKey.id", "$.greetingInputKey.content"})
 *     @PostMapping("/helloworld")
 *     @ResponseBody
 *     public ResponseEntity<Greeting> sayHelloPost(
 *             @RequestBody @TrackingParam(key = "greetingInputKey") Greeting greeting) {
 *         return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, greeting.getContent())));
 *     }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tracking {

    /**
     * In order customizated what elements of inputJson is part of the key, it's possible using this property.
     * and correspond with an Array of JsonPath applied over the inputJson.
     *
     * @return
     */
    String[] hashKey() default {};

}
