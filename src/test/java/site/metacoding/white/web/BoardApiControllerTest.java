package site.metacoding.white.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import site.metacoding.white.domain.Board;
import site.metacoding.white.domain.BoardRepository;
import site.metacoding.white.domain.Comment;
import site.metacoding.white.domain.CommentRepository;
import site.metacoding.white.domain.User;
import site.metacoding.white.domain.UserRepository;
import site.metacoding.white.dto.BoardReqDto.BoardSaveReqDto;
import site.metacoding.white.dto.BoardReqDto.BoardUpdateReqDto;
import site.metacoding.white.dto.SessionUser;
import site.metacoding.white.util.SHA256;

@ActiveProfiles("test") // 테스트 어플리케이션 실행
@Sql("classpath:truncate.sql") // 실행직전에 다 날리는 것
@Transactional // 트랜잭션 안붙이면 영속성 컨텍스트에서 DB로 flush 안됨 (Hibernate 사용시) mybatis는 없어도 된다 -> 롤백할려고 붙임
               // 없어도됨
@AutoConfigureMockMvc // MockMvc Ioc 컨테이너에 등록 실제가 아닌 가짜
@SpringBootTest(webEnvironment = WebEnvironment.MOCK) // MOCK은 가짜 환경임
public class BoardApiControllerTest {

    private static final String APPLICATION_JSON = "application/json; charset=utf-8";

    @Autowired
    private MockMvc mvc; // 이걸로 통신을 한다

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private SHA256 sha256;

    private MockHttpSession session;

    @BeforeEach
    public void sessionInit() {
        session = new MockHttpSession();// 직접 new를 했다 MockHttpSession해야 Mock가 된다
        User user = User.builder().id(1L).username("ssar").build();// password 는 없다
        session.setAttribute("sessionUser", new SessionUser(user));// 가짜세션이 만들어진 상태이다 -> 아직 주입은 안된 상태
    }

    @BeforeEach
    public void dataInit() {
        String encPassword = sha256.encrypt("1234");
        User user = User.builder().username("ssar").password(encPassword).build();
        User userPS = userRepository.save(user);

        Board board = Board.builder()
                .title("스프링1강")
                .content("트랜잭션관리")
                .user(userPS)
                .build();// 트랜잭션이 종료가 안됨// User 와 같이 save 됨
        Board boardPS = boardRepository.save(board);

        Comment comment1 = Comment.builder()
                .content("내용좋아요")
                .board(boardPS)
                .user(userPS)
                .build();

        Comment comment2 = Comment.builder()
                .content("내용싫어요")
                .board(boardPS)
                .user(userPS)
                .build();

        commentRepository.save(comment1);
        commentRepository.save(comment2);
    }

    @Test
    public void save_test() throws Exception {
        // given
        
        BoardSaveReqDto boardSaveReqDto = new BoardSaveReqDto();
        boardSaveReqDto.setTitle("스프링1강");
        boardSaveReqDto.setContent("트랜잭션관리");

        String body = om.writeValueAsString(boardSaveReqDto);

        // when
        ResultActions resultActions = mvc
                .perform(MockMvcRequestBuilders.post("/board").content(body)
                        .contentType(APPLICATION_JSON).accept(APPLICATION_JSON)
                        .session(session));

        // then
        MvcResult mvcResult = resultActions.andReturn();
        System.out.println("디버그 : " + mvcResult.getResponse().getContentAsString());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1L));
    }

    @Test
    public void findById_test() throws Exception {
        // given
        Long id = 1L;

        // when
        ResultActions resultActions = mvc
                .perform(MockMvcRequestBuilders.get("/board/" + id).accept(APPLICATION_JSON));

        // then
        MvcResult mvcResult = resultActions.andReturn();
        System.out.println("디버그 : " + mvcResult.getResponse().getContentAsString());
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("스프링1강"));
    }// 상태코드 200이걸 체크 MockMvcResultMatchers

    @Test//dd
    public void findAll_test() throws Exception {
        // given

        // when
        ResultActions resultActions = mvc
                .perform(MockMvcRequestBuilders.get("/board").accept(APPLICATION_JSON));

        // then
        MvcResult mvcResult = resultActions.andReturn();
        System.out.println("디버그 : " + mvcResult.getResponse().getContentAsString());
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1L));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.data.[0].title").value("스프링1강"));
    }

    @Test
    public void update_test() throws Exception {
        // given
        Long id = 1L;
        BoardUpdateReqDto boardUpdateReqDto = new BoardUpdateReqDto();
        boardUpdateReqDto.setTitle("스프링2강");
        boardUpdateReqDto.setContent("JUNIT공부");

        String body = om.writeValueAsString(boardUpdateReqDto);
        // json 변경

        // when
        ResultActions resultActions = mvc
                .perform(MockMvcRequestBuilders.put("/board/" + id).content(body)
                        // post안에 , 해서 넣을수있다 -> 쿼리스트림
                        .contentType(APPLICATION_JSON).accept(APPLICATION_JSON) // 둘 중 하나라도 안적으면 안나옴 -> json타입인줄 몰라서
                        .session(session)); // 가짜세션!!

        // then/ charset=utf-8안넣으면바로한글이깨진다
        MvcResult mvcResult = resultActions.andReturn();
        System.out.println("디버그 : " + mvcResult.getResponse().getContentAsString());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1L));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("스프링2강"));
    }

    @Test
    public void deleteById_test() throws Exception {
        // given
        Long id = 1L;
        // when
        ResultActions resultActions = mvc
                .perform(MockMvcRequestBuilders.delete("/board/" + id)
                        .accept(APPLICATION_JSON)
                        .session(session));

        // then/ charset=utf-8안넣으면바로한글이깨진다

        MvcResult mvcResult = resultActions.andReturn();
        System.out.println("디버그 : " + mvcResult.getResponse().getContentAsString());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1L));
    }
}