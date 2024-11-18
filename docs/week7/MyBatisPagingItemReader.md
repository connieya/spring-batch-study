# MyBatisPagingItemReader로 DB 내용을 읽고, MyBatisItemWriter 로 DB에 쓰기

## MyBatisItemReader

MyBatisPagingItemReader Spring Mybatis 에서 제공하는 ItemReader 인터페이스를 
구현하는 클래스이다.

MyBatis 의 Object Relation Mapper 는 다음과 같은 특징을 가지고 있다.

### 장점 

- 간편한 설정 : MyBatis 쿼리 매퍼를 직접 활용하여 데이터를 읽을 수 있어 설정이 간편하다.
- 쿼리 최적화 : MyBatis 의 다양한 기능을 활용하여 최적화된 쿼리를 작성할 수 있다.
- 동적 쿼리 지원 : 런타임 시 조건에 따라 동적으로 쿼리를 생성할 수 있다.

### 단점

- MyBatis 의존성 : MyBatis 라이브러리에 의존해야 한다.
- 커스터마이징 복잡 : Chunk-oriented Processing 방식과 비교했을 때 커스터마이징이 더 복잡할 수 있다.

### 주요 구성 요소

- SqlSessionFactory : MyBatis 설정 정보 및 SQL 쿼리 매퍼 정보를 담고 있는 객체이다.
- QueryId : 데이터를 읽을 MyBatis 쿼리 ID 이다.
- PageSize : 페이징 쿼리를 위한 페이지 크기를 지정한다.

#### SqlSessionFactory :

- MyBatisPagingItemReader SqlSessionFactory 객체를 통해 MyBatis 와 연동된다.
- SqlSessionFactory 는 다음과 같은 방법으로 설정할 수 있다.
- @Bean 어노테이션을 사용하여 직접 생성
- Spring Batch XML 설정 파잀에서 설정
- Java 코드에서 직접 설정

#### QueryId :
- MyBatisPagingItemReader setQueryId() 메소드를 통해 데이터를 읽을 MyBatis 쿼리 ID 를 설정한다.
- 쿼리 ID는 com.example.mapper.CustomerMapper.selectCustomers 와 같은 형식으로 지정된다.

#### PageSize :
- MyBatisItemReader 는 pageSize 를 이용하여 offset , limit 을 이용하는 기준을 설정할 수 있다.

#### 추가 구성 요소

- SkippableItemReader : 오류 발생 시 해당 Item 을 건너뛸 수 있도록 한다.
- ReadListener : 읽기 시작, 종료, 오류 발생 등의 이벤트를 처리할 수 있도록 한다.
- SaveStateCallback : 잡의 중단 시 현재 상태를 저장하여 재시작 시 이어서 처리할 수 있도록 한다.

### 샘플 코드

#### 의존성 설정

```groovy
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
```

MyBatisPagingItemReader 를 사용하기 위한 의존성 설정

```groovy
implementation group: 'org.apache.ibatis', name: 'ibatis-core', version: '3.0'
```

sqlSessionFactory 를 사용하기 위한 의존성 설정



#### Customer 작성

```java
public class Customer {

  
    private int id;
    private String name;
    private int age;
    private String gender;
}
```

#### MyBatisPagingItemReader 작성하기

```java
    @Bean
    public MyBatisPagingItemReader<Customer> myBatisPagingItemReader() {
        return new MyBatisPagingItemReaderBuilder<Customer>()
                .sqlSessionFactory(sqlSessionFactory)
                .pageSize(CHUNK_SIZE)
                .queryId("com.study.batch_sample.week7.mapper.CustomerMapper")
                .build();
    }
```

- MyBatisPagingItemReader 를 이용하여 DB 쿼리 결과를 읽을 수 있도록 ItemReader 를 반환한다.
- sqlSessionFactory : 세션 팩토리를 지정한다.
- pageSize : 페이징 단위를 지정한다.
- parameterValues : 파라미터를 전달할 수 있다.


#### query 작성하기

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >

<mapper namespace="com.study.batch_sample.week7.mapper">
    <resultMap id="customerResult" type="com.study.batch_sample.week7.model.Customer">
        <result property="id" column="id"/>
        <result property="name" column="name"/>
        <result property="age" column="age"/>
        <result property="gender" column="gender"/>
    </resultMap>

    <select id="selectCustomers" resultMap="customerResult">
        SELECT id, name, age, gender
        FROM customer
                 LIMIT #{_skiprows}, #{_pagesize}
    </select>
</mapper>
```

- namespace : 쿼리들을 그룹화해서 모아놓은 이름 공간이다.
- resultMap : 결과로 반환할 결과맵을 지정한다. 이는 db 컬럼과 java 필드 이름을 매핑한다.
- select : 쿼리를 지정한다. 
- #{_skiprows} : 오프셋을 이야기하며, 쿼리 결과에서 얼마나 스킵할지 지정한다. pageSize 를 지정했다면 자동으로 계산된다.
- #{_pagesize} : 한번에 가져올 페이지를 지정한다.



### 전체 코드

```java
@Slf4j
@Configuration
public class MyBatisReaderJobConfig {

    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";
    public static final String MYBATIS_CHUNK_JOB = "MYBATIS_CHUNK_JOB";

    @Autowired
    DataSource dataSource;

    SqlSessionFactory sqlSessionFactory;


    @Bean
    public MyBatisPagingItemReader<Customer> myBatisItemReader() {
        return new MyBatisPagingItemReaderBuilder<Customer>()
                .sqlSessionFactory(sqlSessionFactory)
                .pageSize(CHUNK_SIZE)
                .queryId("com.study.batch_sample.week7.mapper.CustomerMapper")
                .build();
    }
    
    @Bean
    public FlatFileItemWriter<Customer> customerCursorFlatFileItemWriter() {
        return new FlatFileItemWriterBuilder<Customer>()
                .name("customerCursorFlatFileItemWriter")
                .resource(new FileSystemResource("./output/customer_new_v4.csv"))
                .encoding(ENCODING)
                .delimited().delimiter("\t")
                .names("Name","Age","Gender")
                .build();
    }
    
    @Bean
    public Step customerJdbcCursorStep(JobRepository jobRepository , PlatformTransactionManager transactionManager) {
        log.info("------------------ Init customerJdbcCursorStep ----------------");
        
        return new StepBuilder("customerJdbcCursorStep",jobRepository)
                .<Customer , Customer>chunk(CHUNK_SIZE , transactionManager)
                .reader(myBatisItemReader())
                .processor(new CustomerItemProcessor())
                .writer(customerCursorFlatFileItemWriter())
                .build();
    }
    
    @Bean
    public Job customerJdbcCursorPagingJob(Step customerJdbcCursorStep , JobRepository jobRepository) {
        log.info("------------------ Init customerJdbcCursorPagingJob ----------------");
        return new JobBuilder(MYBATIS_CHUNK_JOB , jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(customerJdbcCursorStep)
                .build();
    }
    
}
```

