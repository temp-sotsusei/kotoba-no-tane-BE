package io.github.tempsotsusei.kotobanotane.infrastructure.persistence.keyword;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** keywords テーブルに対応する JPA エンティティ。 */
@Entity
@Table(name = "keywords")
public class KeywordEntity {

	/** キーワード ID。 */
	@Id
	@Column(name = "keyword_id", length = 255, nullable = false)
	private String keywordId;

	/** 紐付く章 ID。 */
	@Column(name = "chapter_id", length = 255, nullable = false)
	private String chapterId;

	/** キーワード文字列。 */
	@Column(name = "keyword", length = 255, nullable = false)
	private String keyword;

	/** キーワードの出現文字数。 */
	@Column(name = "keyword_position", nullable = false)
	private int keywordPosition;

	/** 作成日時。 */
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	/** 更新日時。 */
	@Column(name = "updated_at")
	private Instant updatedAt;

	/** JPA が利用するデフォルトコンストラクタ。 */
	protected KeywordEntity() {
	}

	public KeywordEntity(
			String keywordId,
			String chapterId,
			String keyword,
			int keywordPosition,
			Instant createdAt,
			Instant updatedAt) {
		this.keywordId = keywordId;
		this.chapterId = chapterId;
		this.keyword = keyword;
		this.keywordPosition = keywordPosition;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public String getKeywordId() {
		return keywordId;
	}

	public String getChapterId() {
		return chapterId;
	}

	public String getKeyword() {
		return keyword;
	}

	public int getKeywordPosition() {
		return keywordPosition;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * 章 ID・キーワード本文・表示順を変更し、更新日時を反映する。
	 *
	 * @param newChapterId       更新後の章 ID
	 * @param newKeyword         更新後のキーワード本文
	 * @param newKeywordPosition 更新後の表示順
	 * @param newUpdatedAt       更新日時
	 */
	public void updateDetails(
			String newChapterId, String newKeyword, int newKeywordPosition, Instant newUpdatedAt) {
		this.chapterId = newChapterId;
		this.keyword = newKeyword;
		this.keywordPosition = newKeywordPosition;
		this.updatedAt = newUpdatedAt;
	}
}
