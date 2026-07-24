package com.mycom.myapp.domain.room.repository;

import static com.mycom.myapp.domain.room.entity.QRoom.room;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.mycom.myapp.domain.room.entity.Room;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoomRepositoryImpl implements RoomRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Room> search(String name, Integer capacity, Integer price) {
		return queryFactory.selectFrom(room).where(nameContains(name), capacityGoe(capacity), priceLoe(price)).fetch();
	}

	@Override
	public Page<Room> search(String name, Integer capacity, Integer price, Pageable pageable) {
		List<Room> content = queryFactory.selectFrom(room).where(nameContains(name), capacityGoe(capacity), priceLoe(price)).offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();
		Long total = queryFactory.select(room.count()).from(room).where(nameContains(name), capacityGoe(capacity), priceLoe(price)).fetchOne();
		return new PageImpl<>(content, pageable, total != null ? total : 0);
	}

	private BooleanExpression nameContains(String name) {
		return StringUtils.hasText(name) ? room.name.contains(name) : null;
	}

	private BooleanExpression capacityGoe(Integer capacity) {
		return capacity != null ? room.capacity.goe(capacity) : null;
	}

	private BooleanExpression priceLoe(Integer price) {
		return price != null ? room.price.loe(price) : null;
	}
}