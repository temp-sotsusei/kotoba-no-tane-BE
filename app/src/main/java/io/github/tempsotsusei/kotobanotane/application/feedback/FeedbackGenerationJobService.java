package io.github.tempsotsusei.kotobanotane.application.feedback;

import io.github.tempsotsusei.kotobanotane.application.llm.FeedbackGenerationService;
import io.github.tempsotsusei.kotobanotane.application.llm.FeedbackItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 章本文を基にフィードバックを生成し、非同期で保存するジョブサービス。
 *
 * <p>LLM 呼び出しに失敗した場合は、固定の失敗メッセージを保存して空欄を残さない。
 */
@Service
public class FeedbackGenerationJobService {

  private static final Logger log = LoggerFactory.getLogger(FeedbackGenerationJobService.class);
  private static final String FAILURE_MESSAGE = "フィードバック生成に失敗しました。";
  private static final String NO_ISSUE_MESSAGE = "なおすところはなかったよ。";

  private final FeedbackGenerationService feedbackGenerationService;
  private final FeedbackService feedbackService;

  public FeedbackGenerationJobService(
      FeedbackGenerationService feedbackGenerationService, FeedbackService feedbackService) {
    this.feedbackGenerationService = feedbackGenerationService;
    this.feedbackService = feedbackService;
  }

  /**
   * 章本文を平文化した文字列を元にフィードバックを生成し、保存する。
   *
   * @param chapterId 保存先の章 ID
   * @param chapterText 平文化済み章本文
   */
  @Async("llmJobExecutor")
  public void generateAndSave(String chapterId, String chapterText) {
    try {
      var feedbacks = feedbackGenerationService.generate(chapterText);
      String formatted = formatFeedbacks(feedbacks);
      feedbackService.create(chapterId, formatted);
      log.info("Feedback generated for chapterId={}", chapterId);
    } catch (Exception e) {
      log.warn("Feedback generation failed for chapterId={}", chapterId, e);
      feedbackService.create(chapterId, FAILURE_MESSAGE);
    }
  }

  /**
   * フィードバック配列を保存用のテキストに整形する。
   *
   * @param feedbacks original/corrected/reason のリスト
   * @return ラベル付きテキスト（間違いなしの場合は固定メッセージ）
   */
  private String formatFeedbacks(java.util.List<FeedbackItem> feedbacks) {
    if (feedbacks == null || feedbacks.isEmpty()) {
      return NO_ISSUE_MESSAGE;
    }
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < feedbacks.size(); i++) {
      FeedbackItem item = feedbacks.get(i);
      builder
          .append("［ことばそのまま］\n")
          .append(item.original())
          .append("\n［なおしたぶん］\n")
          .append(item.corrected())
          .append("\n［どうして？］\n")
          .append(item.reason());
      if (i < feedbacks.size() - 1) {
        builder.append("\n\n");
      }
    }
    return builder.toString();
  }
}
