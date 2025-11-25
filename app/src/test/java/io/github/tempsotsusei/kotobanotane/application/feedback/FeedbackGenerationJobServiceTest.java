package io.github.tempsotsusei.kotobanotane.application.feedback;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.tempsotsusei.kotobanotane.application.llm.FeedbackGenerationService;
import io.github.tempsotsusei.kotobanotane.application.llm.FeedbackItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FeedbackGenerationJobServiceTest {

  private final FeedbackGenerationService feedbackGenerationService =
      Mockito.mock(FeedbackGenerationService.class);
  private final FeedbackService feedbackService = Mockito.mock(FeedbackService.class);

  private FeedbackGenerationJobService jobService;

  @BeforeEach
  void setUp() {
    jobService = new FeedbackGenerationJobService(feedbackGenerationService, feedbackService);
  }

  /** LLM 成功時に生成されたフィードバックが保存されることを検証する。 */
  @Test
  void savesFeedbackWhenLlmSucceeds() {
    when(feedbackGenerationService.generate("text"))
        .thenReturn(java.util.List.of(new FeedbackItem("もと", "なおし", "りゆう")));

    jobService.generateAndSave("chap-1", "text");

    verify(feedbackService)
        .create(
            "chap-1",
                """
						［ことばそのまま］
						もと
						［なおしたぶん］
						なおし
						［どうして？］
						りゆう"""
                .trim());
  }

  /** LLM 失敗時に固定メッセージが保存されることを検証する。 */
  @Test
  void savesFailureMessageWhenLlmFails() {
    when(feedbackGenerationService.generate("text")).thenThrow(new RuntimeException("llm failure"));

    jobService.generateAndSave("chap-1", "text");

    verify(feedbackService).create("chap-1", "フィードバック生成に失敗しました。");
  }
}
