package cloud.fmunozse.demoaopannotation.web;

import cloud.fmunozse.demoaopannotation.DemoAopAnnotationApplication;
import cloud.fmunozse.demoaopannotation.domain.Tracking;
import cloud.fmunozse.demoaopannotation.repository.TrackingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.DigestUtils;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoAopAnnotationApplication.class)
@AutoConfigureMockMvc
public class HelloWorldControllerIntegrationTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TrackingRepository trackingRepository;

    @Before
    public void setUp() {
        trackingRepository.deleteAll();
    }

    @Test
    public void whenEndpointIsNoTag_thenHashContainsAllinputParams() throws Exception {
        //given

        //when
        mvc.perform(get("/hello-world-noTag")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", is("Hello, Stranger!")));

        System.out.println(trackingRepository.findAll());

        //then
        String hash = generateHash("{\"arg0\":\"Stranger\",\"arg1\":\"defaultValuetest1\"}", "GET", "/hello-world-noTag");
        Optional<Tracking> tracking = trackingRepository.findByHashRequest(hash);
        assertTrue(tracking.isPresent());
    }


    @Test
    public void whenEndpointIsNoTagUsingParam_thenHashContainsAllinputParams() throws Exception {
        //given

        //when
        mvc.perform(get("/hello-world-noTag?name=pepe")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", is("Hello, pepe!")));

        System.out.println(trackingRepository.findAll());

        //then
        String hash = generateHash("{\"arg0\":\"pepe\",\"arg1\":\"defaultValuetest1\"}", "GET", "/hello-world-noTag?name=pepe");
        Optional<Tracking> tracking = trackingRepository.findByHashRequest(hash);
        assertTrue(tracking.isPresent());
    }


    @Test
    public void whenEndpointHas1Tag_thenHashContains1Tag() throws Exception {
        //given

        //when
        mvc.perform(get("/hello-world-1tag")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", is("Hello, Stranger!")));

        System.out.println(trackingRepository.findAll());

        //then
        String hash = generateHash("{\"nameInputKey\":\"Stranger\"}", "GET", "/hello-world-1tag");
        Optional<Tracking> tracking = trackingRepository.findByHashRequest(hash);
        assertTrue(tracking.isPresent());
    }


    @Test
    public void whenEndpointHas2TagWithKey_thenHashContainsTheKey() throws Exception {
        //given

        //when
        mvc.perform(get("/hello-world-2tag")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", is("Hello, Stranger!")));

        System.out.println(trackingRepository.findAll());

        //then
        String hash = generateHash("defaultValuetest1", "GET", "/hello-world-2tag");
        Optional<Tracking> tracking = trackingRepository.findByHashRequest(hash);
        assertTrue(tracking.isPresent());
    }

    @Test
    public void whenEndpointIsPostWithMultiplesKey_thenHashContainsAllKeys() throws Exception {
        //given
        Greeting greetingRequest = new Greeting(1, "pepe");

        //when
        mvc.perform(post("/helloworld")
                .content(objectMapper.writeValueAsBytes(greetingRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", is("Hello, pepe!")));

        System.out.println(trackingRepository.findAll());

        //then
        String hash = generateHash("1#pepe", "POST", "/helloworld");
        Optional<Tracking> tracking = trackingRepository.findByHashRequest(hash);
        assertTrue(tracking.isPresent());
    }


    private String generateHash(String inputKeys, String method, String url) {
        StringBuilder sb = new StringBuilder();
        sb.append(inputKeys).append(method).append(url);
        return DigestUtils.md5DigestAsHex(sb.toString().getBytes());
    }

}