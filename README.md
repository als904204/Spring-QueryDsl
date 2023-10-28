# Spring-QueryDsl

## 1. QueryDSL 설정 (현재 3.1.2 사용중)

---
### 스프링 부트 2.6 ~ 2.7에서 build.gradle 설정
```java
// (1) queryDSL 추가
buildscript {
	ext {
		queryDslVersion = "5.0.0"
	}
}

plugins {
	id 'java'
	id 'org.springframework.boot' version '2.7.7'
	id 'io.spring.dependency-management' version '1.0.15.RELEASE'
	// (2) queryDSL 추가
	id "com.ewerk.gradle.plugins.querydsl" version "1.0.10"
}

group = 'study'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-web'

	// (3) queryDSL 추가
	implementation "com.querydsl:querydsl-jpa:${queryDslVersion}"
	annotationProcessor "com.querydsl:querydsl-apt:${queryDslVersion}"

	compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'com.h2database:h2'
	annotationProcessor 'org.projectlombok:lombok'

	testCompileOnly 'org.projectlombok:lombok'
	testAnnotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
	useJUnitPlatform()
}

// (4) queryDSL 추가 : QueryDSL 빌드 옵션
def querydslDir = "$buildDir/generated/querydsl"

querydsl {
	jpa = true
	querydslSourcesDir = querydslDir
}
sourceSets {
	main.java.srcDir querydslDir
}
configurations {
	querydsl.extendsFrom compileClasspath
}
compileQuerydsl {
	options.annotationProcessorPath = configurations.querydsl
}
// 여기까지
```


---

### 스프링 부트 3.0에서 build.gradle 설정
```java
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.1.2'
    id 'io.spring.dependency-management' version '1.1.2'

    //  queryDsl
    id "com.ewerk.gradle.plugins.querydsl" version "1.0.10"
}


group = 'com.crud'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity6'
    compileOnly 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    runtimeOnly 'com.h2database:h2'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'

    // Querydsl 추가
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jakarta"
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"
}

tasks.named('test') {
    useJUnitPlatform()
}

// queryDSL 추가 : QueryDSL 빌드 옵션
def querydslDir = "$buildDir/generated/querydsl"

querydsl {
    jpa = true
    querydslSourcesDir = querydslDir
}
sourceSets {
    main.java.srcDir querydslDir
}
configurations {
    querydsl.extendsFrom compileClasspath
}
compileQuerydsl {
    options.annotationProcessorPath = configurations.querydsl
}

```

---
## 2. 환경설정 테스트
```java
@Entity
@Getter
@Setter
public class Hello {

    @Id
    @GeneratedValue
    private Long id;
}

```

### 검증용 테스트 Q타입 생성

- Gradle -> Tasks -> others -> complieQuerydsl
  - build/generated/{프로젝트명}
  - build/generated/querydsl/com/crud/querydsl/utils/hello/entity/QHello.java
![img.png](img.png)


----

## 3. 테스트코드
```java
package com.crud.querydsl;

import static com.crud.querydsl.domain.team.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

import com.crud.querydsl.dto.member.MemberDto;
import com.crud.querydsl.domain.member.dto.QMemberDto;
import com.crud.querydsl.entity.member.Member;

import com.crud.querydsl.entity.team.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
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
   * 1. JPQL 을 사용하여 member1 조회 직접 String 형식으로 SQL 문을 작성해야하는 단점 런타임 시에 오류 발생 X
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
    em.persist(new Member(null, 100));
    em.persist(new Member("member5", 100));
    em.persist(new Member("member6", 100));

    List<Member> resultList = queryFactory
            .selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(),
                    member.username.asc().nullsLast()) // age 순으로 내림차순, 같다면 이름순으로 오름차순
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

  @DisplayName("left join")
  @Test
  public void leftJoin() {
    /**
     * 회원은 모두 조회하면서
     * 회원이 소속된 팀 이름이 teamA인 회원만 팀까지 표시
     * SELECT m.*, t.*
     * FROM MEMBER m
     * LEFT JOIN TEAM t
     * ON m.TEAM_ID = t.id and t.name="teamA"
     */

    List<Tuple> fetch = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(member.team, team).on(team.name.eq("teamA"))
            .fetch();

    for (Tuple t : fetch) {
      System.out.println("t = " + t);
    }

  }

  @DisplayName("Projection 타입이 한개")
  @Test
  public void projectionOnlyOneType() {
    List<String> result = queryFactory
            .select(member.username)
            .from(member)
            .fetch();

    for (String s : result) {
      System.out.println("s = " + s);
    }
  }

  @DisplayName("Projection 타입이 여러개")
  @Test
  public void projectionMultipleType() {
    List<Tuple> result = queryFactory
            .select(member.username, member.age)
            .from(member)
            .fetch();

    for (Tuple t : result) {
      String username = t.get(member.username);
      Integer age = t.get(member.age);
      System.out.println("username = " + username);
      System.out.println("age = " + age);
    }
  }

  @DisplayName("Projection Setter 를 이용해 Dto 로 받기")
  @Test
  public void projectionGetDtoSetter() {
    List<MemberDto> result = queryFactory
            .select(Projections.bean(MemberDto.class,
                    member.username,
                    member.age))
            .from(member)
            .fetch();

    for (MemberDto m: result) {
      System.out.println("dto = "+m);
    }
  }

  @DisplayName("Projection Fields 를 이용해 Dto 로 받기")
  @Test
  public void projectionGetDtoFields() {
    List<MemberDto> result = queryFactory
            .select(Projections.fields(MemberDto.class,
                    member.username,
                    member.age))
            .from(member)
            .fetch();

    for (MemberDto m: result) {
      System.out.println("dto = "+m);
    }
  }

  @DisplayName("QueryProjection 을 이용해 Dto 로 받기")
  @Test
  public void projectionGetDtoQueryProjection() {
    List<MemberDto> result = queryFactory
            .select(new QMemberDto(member.username, member.age))
            .from(member)
            .fetch();

    for (MemberDto dto : result) {
      System.out.println(dto);
    }
  }

}
```

> 쿼리 완성 후 결과 조회 시에는 다음과 같은 메서드 들을 이용 가능
- fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
  - fetchOne() : 단 건 조회, 결과 없으면 null, 2개 이상이라면 NonUniqueResultException
- fetchFirst() : 첫 번째 결과 조회
- fetchResults() : 페이징 정보 포함 및 totalCount 쿼리를 추가 실행
- fetchCount() : count 쿼리로 변경해서 count 수를 조회 할 수 있다