package com.mysite.sbb;


import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerRepository;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionRepository;
import com.mysite.sbb.question.QuestionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SbbApplicationTests {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	QuestionService questionService;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private AnswerRepository answerRepository;

	private Integer question1Id; // 첫 번째 질문의 ID 저장
	private Integer question2Id; // 두 번째 질문의 ID 저장

	@BeforeEach
	void setUp() {
		answerRepository.deleteAll();
		questionRepository.deleteAll();

		question1Id = createAndSaveQuestion("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.").getId();
		question2Id = createAndSaveQuestion("스프링부트 모델 질문입니다.", "id는 자동으로 생성되나요?").getId();
	}

	private Question createAndSaveQuestion(String subject, String content) {
		Question question = new Question();
		question.setSubject(subject);
		question.setContent(content);
		question.setCreateDate(LocalDateTime.now());
		return questionRepository.save(question);
	}

	@Test
	@DisplayName("모든 질문 조회 테스트")
	void testJpaFindAllQuestions() {
		List<Question> questions = questionRepository.findAll();
		assertEquals(2, questions.size());
		assertEquals("sbb가 무엇인가요?", questions.get(0).getSubject());
	}

	@Test
	@DisplayName("모든 질문 조회 후 특정 질문의 제목 가져오기")
	void testJpaFindAllQuestionsAndGetSingleQuestionSubject() {
		List<Question> all = this.questionRepository.findAll();
		assertEquals(2, all.size());

		Question q = all.get(0);
		assertEquals("sbb가 무엇인가요?", q.getSubject());
	}

	@Test
	@DisplayName("제목으로 단일 질문 조회 테스트")
	void testJpaFindSingleQuestionsWithSubject() {
		String subject = "sbb가 무엇인가요?";
		Question question = this.questionRepository.findBySubject(subject);

		assertEquals(subject, question.getSubject());
	}

	@Test
	@DisplayName("제목과 내용으로 질문 조회 테스트")
	void testJpaFindQuestionWithSubjectAndContent() {
		String subject = "sbb가 무엇인가요?";
		String content = "sbb에 대해서 알고 싶습니다.";
		Question question = this.questionRepository.findBySubjectAndContent(subject, content);

		assertEquals(subject, question.getSubject());
		assertEquals(content, question.getContent());
	}

	@Test
	@DisplayName("제목을 포함하는 질문 조회 테스트")
	void testJpaFindQuestionWithSubjectLike(){
		String subject = "sbb가 무엇인가요?";
		String content = "sbb에 대해서 알고 싶습니다.";
		List<Question> questions = this.questionRepository.findBySubjectLike("sbb%");
		Question question = questions.get(0);
		assertEquals(subject, question.getSubject());
	}

	@Test
	@DisplayName("ID로 질문 수정 테스트")
	void testJpaUpdateQuestionById() {
		// 저장된 ID를 사용하여 질문 조회
		Optional<Question> question = this.questionRepository.findById(question1Id);
		assertTrue(question.isPresent());

		Question q = question.get();
		String subjectToUpdate = "제목 수정";
		q.setSubject(subjectToUpdate);
		this.questionRepository.save(q);

		// 변경 확인
		Optional<Question> updatedQuestion = this.questionRepository.findById(question1Id);
		assertTrue(updatedQuestion.isPresent());
		assertEquals(subjectToUpdate, updatedQuestion.get().getSubject());
	}


	@Test
	@DisplayName("ID를 이용한 특정 질문 삭제 테스트")
	void testJpaDeleteSpecificQuestionWithId(){
		assertEquals(2, questionRepository.count());
		Optional<Question> optionalQuestion = questionRepository.findById(question1Id);

		assertTrue(optionalQuestion.isPresent());
		Question question = optionalQuestion.get();
		questionRepository.delete(question);
		assertEquals(1, questionRepository.count());
	}

	@Test
	@DisplayName("질문에 대한 답변 데이터 생성")
	void testJpaCreateAnswerWithTheQuestion(){
		Optional<Question> optionalQuestion = this.questionRepository.findById(question2Id);
		assertTrue(optionalQuestion.isPresent());
		Question question = optionalQuestion.get();

		Answer answer = new Answer();
		String content = "네 자동으로 생성됩니다.";
		answer.setContent(content);
		answer.setQuestion(question);  // 어떤 질문의 답변인지 알기위해서 Question 객체가 필요하다.
		answer.setCreateDate(LocalDateTime.now());
		this.answerRepository.save(answer);
	}

	@Test
	@DisplayName("답변 생성 및 조회하기")
	void testJpaCreateAndRetrieveAnswer(){

		Optional<Question> optionalQuestion = this.questionRepository.findById(question2Id);
		assertTrue(optionalQuestion.isPresent());
		Question question = optionalQuestion.get();

		Answer answer = new Answer();
		String content = "네 자동으로 생성됩니다.";
		answer.setContent(content);
		answer.setQuestion(question);  // 어떤 질문의 답변인지 알기위해서 Question 객체가 필요하다.
		answer.setCreateDate(LocalDateTime.now());
		Integer answerId = this.answerRepository.save(answer).getId();

		Optional<Answer> optionalAnswer = this.answerRepository.findById(answerId);
		assertTrue(optionalAnswer.isPresent());
		Answer queriedAnswer = optionalAnswer.get();
		assertEquals(question2Id, queriedAnswer.getQuestion().getId());
	}

	@Test
	@Transactional
	@DisplayName("질문의 답변 리스트 조회하기")
	void tesetJpaFindAllAnswerwithTheQuestion(){
		// 특정 ID에 대한 Question 엔티티와 그 답변들을 함께 로드
		Optional<Question> optionalQuestion = this.questionRepository.findById(question2Id);
		assertTrue(optionalQuestion.isPresent());
		Question question = optionalQuestion.get();

		// 답변 생성 및 저장
		Answer answer = new Answer();
		String content = "네 자동으로 생성됩니다.";
		answer.setContent(content);
		answer.setQuestion(question);
		answer.setCreateDate(LocalDateTime.now());
		this.answerRepository.save(answer);

		entityManager.flush();
		entityManager.clear();
		// 변경된 데이터 반영을 위해 질문을 다시 조회
		Question updatedQuestion = this.questionRepository.findById(question2Id).get();

		// 답변 리스트 확인
		List<Answer> updatedAnswerList = updatedQuestion.getAnswerList();
		assertEquals(1, updatedAnswerList.size());
		assertEquals(content, updatedAnswerList.get(0).getContent());
	}

	@Test
	@DisplayName("테스트 데이터 300개 생성")
	void testCreate300TempData(){
		for (int i = 1; i<= 300; i++){
			String subject = String.format("테스트 데이터입니다:[%03d]", i);
			String content = "내용무";
			this.questionService.create(subject, content);
		}
	}
}
