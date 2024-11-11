# JpaPagingItemReader로 DB 내용을 읽고, JpaItemWriter 로 DB에 쓰기

## JpaPagingItemReader 개요

- JpaPagingItemReader 는 Spring Batch 에서 제공하는 ItemReader 로 , JPA 를 사용하여 데이터베이스로부터 데이터를 페이지 단위로 읽는다.
- JPA 기능 활용 : JPA 엔티티 기반 데이터 처리, 객체 매핑 자동화 등 JPA 의 다양한 기능을 활용할 수 있다.
- 쿼리 최적화 : JPA 쿼리 기능을 사용하여 최적화된 데이터 읽기가 가능하다.
- 커서 제어 : JPA Criteria API 를 사용하여 데이터 순회를 제어할 수 있다.

## JpaPagingItemReader 주요 구성 요소

- EntityManagerFactory : JPA 엔티티 매니저 팩토리를 설정한다.
- JpaQueryProvider : 데이터를 읽을 JPA 쿼리를 제공한다.
- PageSize : 페이지 크기를 설정한다.
- SkippableItemReader : 오류 발생 시 해당 Item 을 건너뛸 수 있도록 한다.
- ReadListener : 읽기 시작, 종료 , 오류 발생 등의 이벤트를 처리할 수 있도록 한다.
- SaveStateCallback : 잠시 중단 시 현재 상태를 젖아하여 재시작 시 이어서 처리할 수 있도록 한다. 

## Jpa 설정

- gradle

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```

## JpaPagingItemReader 샘플 코드 

### Customer 클래스 

```java
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String name;
    private int age;
    private String gender;
}

```

### JpaPagingItemReader 이용하기

#### JpaPagingItemReader 생성자를 이용한 방법

````java
    @Bean
    public JpaPagingItemReader<Customer> customerPagingItemReader() {
        JpaPagingItemReader<Customer> jpaPagingItemReader = new JpaPagingItemReader<>();
        jpaPagingItemReader.setQueryString(
                "Select c from CUSTOMER c where c.age > :age order by id desc"
        );
        jpaPagingItemReader.setEntityManagerFactory(entityManagerFactory);
        jpaPagingItemReader.setPageSize(CHUNK_SIZE);
        jpaPagingItemReader.setParameterValues(Collections.singletonMap("age",20));
        return jpaPagingItemReader;
    }
````

- 가장 단순하게 JpaPagingItemReader 를 생성하고 이를 사용
- setQueryString : JPQL 쿼리를 이용하였다.  c.age > :age 를 통해서 파라미터를 추가했다.
- setEntityManagerFactory : JPA 를 위한 엔티티 매니저를 지정했다.
- setPageSize : 한번에 읽어올 페이지 크기를 잡았다. 이 경우 청크 크기와 맞춰주는 것이 일반적이다.
- setParameterValues : JPQL 쿼리에 전달할 파라미터를 지정했다.

#### JpaPagingItemReaderBuilder 이용한 방법

````java
    @Bean
    public JpaPagingItemReader<Customer> customerJpaPagingItemReader() {
        return new JpaPagingItemReaderBuilder<Customer>()
                .name("customerJpaPagingItemReader")
                .queryString("select c from CUSTOMER c where c.age > :age order by id desc")
                .pageSize(CHUNK_SIZE)
                .entityManagerFactory(entityManagerFactory)
                .parameterValues(Collections.singletonMap("age",20))
                .build();
    }

````

- 생성자 방식과 동일하다.
- 단지 다른 것은 builder 를 이용하였다는 것이 차이점이다.

### 전체코드

```java
@Slf4j
@Configuration
public class JpaPagingReaderJobConfig {

    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";
    public static final String JPA_PAGING_CHUNK_JOB = "JPA_PAGING_CHUNK_JOB";

    @Autowired
    DataSource dataSource;

    @Autowired
    EntityManagerFactory entityManagerFactory;


//    @Bean
//    public JpaPagingItemReader<Customer> customerJpaPagingItemReader() {
//        JpaPagingItemReader<Customer> jpaPagingItemReader = new JpaPagingItemReader<>();
//        jpaPagingItemReader.setQueryString(
//                "Select c from CUSTOMER c where c.age > :age order by id desc"
//        );
//        jpaPagingItemReader.setEntityManagerFactory(entityManagerEntity);
//        jpaPagingItemReader.setPageSize(CHUNK_SIZE);
//        jpaPagingItemReader.setParameterValues(Collections.singletonMap("age",20));
//        return jpaPagingItemReader;
//    }

    @Bean
    public JpaPagingItemReader<Customer> customerJpaPagingItemReader() {
        return new JpaPagingItemReaderBuilder<Customer>()
                .name("customerJpaPagingItemReader")
                .queryString("select c from Customer c where c.age > :age order by id desc")
                .pageSize(CHUNK_SIZE)
                .entityManagerFactory(entityManagerFactory)
                .parameterValues(Collections.singletonMap("age",20))
                .build();
    }

    @Bean
    public FlatFileItemWriter<Customer> customerJpaFlatFileItemWriter() {

        return new FlatFileItemWriterBuilder<Customer>()
                .name("customerJpaFlatFileItemWriter")
                .resource(new FileSystemResource("./output/customer_new_v2.csv"))
                .encoding(ENCODING)
                .delimited().delimiter("\t")
                .names("Name", "Age", "Gender")
                .build();
    }

    @Bean
    public Step customerJpaPagingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        log.info("------------------ Init customerJpaPagingStep -----------------");

        return new StepBuilder("customerJpaPagingStep", jobRepository)
                .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager)
                .reader(customerJpaPagingItemReader())
                .processor(new CustomerItemProcessor())
                .writer(customerJpaFlatFileItemWriter())
                .build();
    }

    @Bean
    public Job customerJpaPagingJob(Step customerJdbcPagingStep, JobRepository jobRepository) {
        log.info("------------------ Init customerJpaPagingJob -----------------");
        return new JobBuilder(JPA_PAGING_CHUNK_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(customerJdbcPagingStep)
                .build();
    }


}

```

## JpaItemWriter 

- JpaItemWriter 는 Spring Batch 에서 제공하는 ItemWriter 인터페이스를 구현한 클래스이다.
- 데이터를 JPA 를 통해 데이터베이스에 저장하는 데 사용된다.

### JpaItemWriter 구성 요소

- EntityManagerFactory : JPA EntityManager 생성을 위한 팩토리 객체
- JpaQueryProvider : 저장할 엔티티를 위한 JPA 쿼리를 생성하는 역할

#### 장점
- ORM 연동 : JPA 를 통해 다양한 데이터베이스에 데이터를 저장할 수 있다.
- 객체 매핑 : 엔티티 객체를 직접 저장하여 코드 간결성을 높일 수 있다.
- 유연성 : 다양한 설정을 통해 원하는 방식으로 데이터를 저장할 수 있다.

#### 단점

- 설정 복잡성 : JPA 설정 및 쿼리 작성이 복잡할 수 있다.
- 데이터베이스 종속 : 특정 데이터베이스에 종속적이다.
- 오류 가능성 : 설정 오류시 데이터 손상 가능성이 있다.


### 전체 코드

```java
@Slf4j
@Configuration
public class JpaPagingWriterJobConfig {

    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";
    public static final String JPA_ITEM_WRITER_JOB = "JPA_ITEM_WRITER_JOB";

    @Autowired
    DataSource dataSource;

    @Autowired
    EntityManagerFactory entityManagerFactory;


    @Bean
    public FlatFileItemReader<Customer> flatFileItemReader() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("FlatFileItemReader")
                .resource(new ClassPathResource("./customers.csv"))
                .encoding(ENCODING)
                .linesToSkip(1)
                .delimited().delimiter(",")
                .names("name","age","gender")
                .targetType(Customer.class)
                .build();
    }

    @Bean
    public JpaItemWriter<Customer> jpaItemWriter() {
        return new JpaItemWriterBuilder<Customer>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }


    @Bean
    public Step flatFileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("------------------ Init flatFileStep -----------------");

        return new StepBuilder("flatFileStep", jobRepository)
                .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager)
                .reader(flatFileItemReader())
                .writer(jpaItemWriter())
                .build();
    }



    @Bean
    public Job flatFileJob(Step flatFileStep, JobRepository jobRepository) {
        log.info("------------------ Init flatFileJob -----------------");
        return new JobBuilder(JPA_ITEM_WRITER_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(flatFileStep)
                .build();
    }


}

```