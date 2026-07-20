package com.mycom.myapp.domain.room.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE room SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(nullable = false, length = 255)
	private String location;

	@Column(nullable = false)
	private Integer capacity;

	@Column(nullable = false)
	private Integer price;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;

	@Builder
	public Room(String name, String location, Integer capacity, Integer price) {
		this.name = name;
		this.location = location;
		this.capacity = capacity;
		this.price = price;
	}

	public void update(String name, String location, Integer capacity, Integer price) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("이름은 비어있을 수 없습니다.");
		}
		if (location == null || location.isBlank()) {
			throw new IllegalArgumentException("지역은 비어있을 수 없습니다.");
		}
		if (capacity == null || capacity <= 0) {
			throw new IllegalArgumentException("수용 인원은 1명 이상이어야 합니다.");
		}
		if (price == null || price < 0) {
			throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
		}

		this.name = name;
		this.location = location;
		this.capacity = capacity;
		this.price = price;
	}
}
