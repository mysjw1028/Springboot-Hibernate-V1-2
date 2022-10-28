package site.metacoding.white.web;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import site.metacoding.white.dto.UserReqDto.JoinReqDto;
import site.metacoding.white.dto.UserReqDto.LoginReqDto;
import site.metacoding.white.service.UserService;

@ActiveProfiles("test")
// @Transactional // 통합테스트에서 RANDOM_PORT를 사용하면 새로운 스레드로 돌기 때문에 rollback 무의미
@Sql("classpath:truncate.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserApiControllerTest {

    @Autowired
    private TestRestTemplate rt;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private UserService userService; // 이렇게해야 controller 안때림

    private static HttpHeaders headers;

    @BeforeAll
    public static void init() {
        headers = new HttpHeaders(); // http 요청 header에 필요
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    // @Order(1)
    @Test
    public void join_test() throws JsonProcessingException {
        // given
        JoinReqDto joinReqDto = new JoinReqDto();
        joinReqDto.setUsername("very");
        joinReqDto.setPassword("1234");

        String body = om.writeValueAsString(joinReqDto);
        System.out.println(body);

        // when
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = rt.exchange("/join", HttpMethod.POST,
                request, String.class);// "/join"를 때림

        // then
        // System.out.println(response.getStatusCode());
        // System.out.println(response.getBody());

        DocumentContext dc = JsonPath.parse(response.getBody());
        // System.out.println(dc.jsonString());
        Integer code = dc.read("$.code");
        Assertions.assertThat(code).isEqualTo(1);
    }

    @Test
    public void login_test() throws JsonProcessingException {
        // data init
        JoinReqDto joinReqDto = new JoinReqDto();
        joinReqDto.setUsername("very");
        joinReqDto.setPassword("1234");
        userService.save(joinReqDto);// 위에서 검증해서 잘 됐으니 얘는 검증할 필요가 없다 무조건 save가 됨!!

        // given
        LoginReqDto loginReqDto = new LoginReqDto();
        loginReqDto.setUsername("very");
        loginReqDto.setPassword("1234");
        String body = om.writeValueAsString(loginReqDto);
        System.out.println("디버그 : " + body);
        // when
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = rt.exchange("/login", HttpMethod.POST,
                request, String.class);// "/join"를 때림
        System.out.println("디버그 : " + response.getBody());
        // then
        DocumentContext dc = JsonPath.parse(response.getBody());
        Integer code = dc.read("$.code");
        String username = dc.read("$.data.username");
        Assertions.assertThat(code).isEqualTo(1);
        Assertions.assertThat(username).isEqualTo("very");// expected : 기대하는 값
    }
}// inset 로직하고 login
