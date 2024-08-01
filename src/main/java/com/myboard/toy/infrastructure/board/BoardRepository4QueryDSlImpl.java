package com.myboard.toy.infrastructure.board;

import com.myboard.toy.domain.board.BoardSearchCondition;
import com.myboard.toy.domain.board.QBoard;
import com.myboard.toy.domain.board.dto.BoardPageDTO;
import com.myboard.toy.domain.reply.QReply;
import com.myboard.toy.securityproject.domain.entity.QAccount;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

public class BoardRepository4QueryDSlImpl implements BoardRepository4QueryDSl{

    private final JPAQueryFactory queryFactory;

    QAccount account = new QAccount(QAccount.account);
    QBoard board = new QBoard(QBoard.board);
    QReply reply = new QReply(QReply.reply);

    public BoardRepository4QueryDSlImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<BoardPageDTO> searchWithPage(BoardSearchCondition condition, Pageable pageable) {

        //BooleanExpression whereClause = condition.getTitle() != null ? titleEq(condition.getTitle()) : null;

        List<BoardPageDTO> query = queryFactory
                .select(Projections.constructor(BoardPageDTO.class,
                        board.id,
                        board.title,
                        board.account.username,
                        board.replies.size().longValue()))
                .from(board)
                .leftJoin(board.replies,reply)
                .where(titleEq(condition.getTitle()))

                .groupBy(board.id)
                .orderBy(board.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(board.count())
                .from(board)
                //.where(whereClause)
                .where(titleEq(condition.getTitle()))
                .fetchOne();

        return new PageImpl<>(query,pageable,total);

    }
    //검색 조건
    private BooleanExpression titleEq(String title) {
        if (StringUtils.hasText(title)) {
            return board.title.contains(title);
        } else {
            return null;
        }
    }
}
