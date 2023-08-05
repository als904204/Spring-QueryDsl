package com.crud.querydsl;

import static com.crud.querydsl.domain.team.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

import com.crud.querydsl.domain.member.entity.Member;

import com.crud.querydsl.domain.team.entity.Team;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static com.crud.querydsl.domain.member.entity.QMember.*;

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

    @Test
    public void startQuerydsl() {

        // username : member1
        Member username = queryFactory
            .select(member)
            .from(member)
            .where(member.username.eq("member1"))
            .fetchOne();

        assertThat(username.getUsername()).isEqualTo("member1");


        // username : member1, age : 10
        // but using and op
        Member usernameAndAge1 = queryFactory
            .select(member)
            .from(member)
            .where(member.username.eq("member1").and(member.age.eq(10)))
            .fetchOne();

        assertThat(usernameAndAge1.getAge()).isEqualTo(10);

        // age : 10
        // but using , op
        Member usernameAndAge2 = queryFactory
            .select(member)
            .from(member)
            .where(member.username.eq("member1"), (member.age.eq(10)))
            .fetchOne();

        assertThat(usernameAndAge2.getAge()).isEqualTo(10);
    }

    @DisplayName("정렬")
    @Test
    public void sort() {
        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));

        List<Member> resultList = queryFactory
            .selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(), member.username.asc().nullsLast()) // age 순으로 내림차순, 같다면 이름순으로 오름차순
            .fetch();

        Member member5 = resultList.get(0);
        Member member6 = resultList.get(1);
        Member memberNull = resultList.get(2);

        assertThat(member5.getAge()).isEqualTo(100);
        assertThat(member6.getAge()).isEqualTo(100);
        assertThat(memberNull.getAge()).isEqualTo(100);
    }

    @DisplayName("페이징 처리쿼리")
    @Test
    public void paging() {
        List<Member> result = queryFactory
            .selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1) // 시작 index
            .limit(2)  // 결과물 수 제한
            .fetch();

        assertThat(result.size()).isEqualTo(2);

        int totalSize = queryFactory
            .selectFrom(member)
            .fetch()
            .size();

        assertThat(totalSize).isEqualTo(4);

    }

    @DisplayName("groupBy")
    @Test
    public void groupBy() {
        List<Tuple> result = queryFactory
            .select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team)
            .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(20);
    }

    @DisplayName("join")
    @Test
    public void join() {
        /**
         * SELECT member
         * from member
         * join member.teamId = team.teamId
         * where team.name = "teamA";
         */
        List<Member> result = queryFactory
            .selectFrom(member)
            .join(member.team, team)
            .where(team.name.eq("teamA"))
            .fetch();

        assertThat(result)
            .extracting("username") // username 컬럼만 추출
            .containsExactly("member1", "member2"); // 컬럼이 순서대로 member1,member2 와 일치하는지 검증
    }

}