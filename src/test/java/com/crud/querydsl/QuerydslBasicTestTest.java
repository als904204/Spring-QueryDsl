package com.crud.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.crud.querydsl.domain.member.entity.Member;
import com.crud.querydsl.domain.team.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class QuerydslBasicTestTest {


    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 20, teamB);
        Member member4 = new Member("member4", 20, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    /**
     * 1. JPQL 을 사용하여 member1 조회
     * 직접 String 형식으로 SQL 문을 작성해야하는 단점
     * 런타임 시에 오류 발생 X
     */
    @Test
    public void startJPQL() {
        Member findByJPQL = em.createQuery(
                "select m "
                    + "from Member m"
                    + " where m.username = :username",
                Member.class)
            .setParameter("username", "member1")
            .getSingleResult();

        assertThat(findByJPQL.getUsername()).isEqualTo("member1");

    }
}