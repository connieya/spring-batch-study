#  입맛에 맞는 배치 처리를 위한 Custom ItemReader/ItemWriter 구현방법 알아보기

## QuerydslPagingItemReader 개요

-  Querydsl 은 SpringBatch 의 공식 ItemReader 가 아니다.
- AbstractPagingItemReader 를 이용하여 Querydsl 을 활용할 수 있도록 ItemReader 를 만든다.
- Querydsl 기능 활용 : Querydsl 의 강력하고 유연한 쿼리 기능을 사용하여 데이터를 효율적으로 읽을 수 있다.
- JPA 엔티티 추상화 : JPA 엔티티에 직접 의존하지 않고 추상화된 쿼리를 작성하여 코드 유지 관리성을 높일 수 있다.
- 동적 쿼리 지원 : 런타임 시 조건에 따라 동적으로 쿼리를 생성할 수 있다.


## QuerydslPagingItemReader 생성하기

```java
public class QuerydslPagingItemReader<T> extends AbstractPagingItemReader<T> {
    
    private EntityManager em;
    private final Function<JPAQueryFactory , JPAQuery<T>> querySupplier;
    
    private final boolean alwaysReadFromZero;

    public QuerydslPagingItemReader(EntityManagerFactory entityManagerFactory, Function<JPAQueryFactory, JPAQuery<T>> querySupplier, int chunkSize) {
        this(ClassUtils.getShortName(QuerydslPagingItemReader.class) , entityManagerFactory , querySupplier , chunkSize , false);
    }


    public QuerydslPagingItemReader(String name , EntityManagerFactory entityManagerFactory , Function<JPAQueryFactory ,JPAQuery<T>> querySupplier , int chunkSize , boolean alwaysReadFromZero) {
        super.setPageSize(chunkSize);
        setName(name);
        this.querySupplier =  querySupplier;
        this.em = entityManagerFactory.createEntityManager();
        this.alwaysReadFromZero = alwaysReadFromZero;
    }

    @Override
    protected void doClose() throws Exception {
        if (em != null) {
            em.close();
        }
        super.doClose();
    }

    @Override
    protected void doReadPage() {
        initQueryResult();
        
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        long offset = 0;
        if (!alwaysReadFromZero) {
            offset = (long) getPage() * getPageSize();
        }
        
        JPAQuery<T> query = querySupplier.apply(jpaQueryFactory).offset(offset).limit(getPageSize());

        List<T> queryResult = query.fetch();
        for (T entity : queryResult) {
            em.detach(entity);
            results.add(entity);
            
        }
    }
        
    
    private void initQueryResult() {
        if (CollectionUtils.isEmpty(results)) {
            results = new CopyOnWriteArrayList<>();
        }else {
            results.clear();
        }
    }
}
```

- AbstractPagingItemReader 는 어댑터 패턴으로 ,상속받는 쪽은 doReadPage 만 구현하면 된다.


#### 생성자

```java
public QuerydslPagingItemReader(String name , EntityManagerFactory entityManagerFactory , Function<JPAQueryFactory ,JPAQuery<T>> querySupplier , int chunkSize , boolean alwaysReadFromZero) {
    super.setPageSize(chunkSize);
    setName(name);
    this.querySupplier =  querySupplier;
    this.em = entityManagerFactory.createEntityManager();
    this.alwaysReadFromZero = alwaysReadFromZero;
}
```

- name : ItemReader 를 구분하기 위한 이름이다.
- entityManagerFactory : JPA 를 이용하기 위해서 entityManagerFactory 를 전달한다.
- Function<JPAQueryFactory , JPAQuery> : JPAQuery 를 생성하기 위한 Functional Interface 이다.
  - 입력 파라미터로 JPAQueryFactory 를 입력으로 전달 받는다.
  - 반환값은 JPAQuery 형태의 queryDSL 쿼리가 된다.
- chunkSize : 한번에 페이징 처리할 페이지 크기다.
- alwaysReadFromZero : 항상 0부터 페이징을 읽을지 여부를 지정한다. 
만약 paging 처리된 데이터 자체를 수정하는 경우 배치처리 누락이 발생할 수 있으므로 이를 해결하기 위한 방안으로 사용된다.

#### doClose()
- doClose 는 기본적으로 AbstractPagingItemReader 에 자체 구현되어 있찌만, EntityManager 자원을 해제하기 위해서 em.close() 를 수행한다.

#### doReadPage()

- 구현해야할 추상 메소드 이다.

```java
@Override
protected void doReadPage() {
        initQueryResult();

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        long offset = 0;
        if (!alwaysReadFromZero) {
            offset = (long) getPage() * getPageSize();
        }

        JPAQuery<T> query = querySupplier.apply(jpaQueryFactory).offset(offset).limit(getPageSize());

        List<T> queryResult = query.fetch();
        for (T entity : queryResult) {
            em.detach(entity);
            results.add(entity);
        }
}
```

- JPAQueryFactory 를 통해서 함수형 인터페이스로 지정된 queryDSL 에 적용할 QueryFactory 이다.
- 만약 alwaysReadFromZero 가 false 라면 offset 과 limit 을 계속 이동하면서 조회하도록 offset 을 계산한다.
- querySupplier.apply
  - 우리가 제공한 querySupplier 에 JPAQueryFactory 를 적용하여 JPAQuery 를 생성하도록 한다.
  - 페이징을 위해서 offset ,limit 을 계산된 offset 과 pageSize 를 지정하여 페이징 처리하도록 한다.
- fetch :
  - 결과를 패치하여 패치된 내역을 result 에 담는다.
  - 이때 entityManager 에서 detach 하여 변경이 실제 DB에 반영되지 않도록 영속성 객체에서 제외시킨다.
- initQueryResult
  - 매 페이징 결과를 반환할 때 페이징 결과만 반환하기 위해서 초기화 한다.
  - 만약 결과객체가 초기화 되어 있지 않다면 CopyOnWriteArrayList 객체를 신규로 생성한다.


## 편의를 위해서 Builder 생성하기

- 위 생성자는 복잡하기 때문에 이를 편하게 작성하기 위한 빌더를 생성해보자.

```java
public class QuerydslPagingItemReaderBuilder<T> {

    private EntityManagerFactory entityManagerFactory;
    private Function<JPAQueryFactory , JPAQuery<T>> querySupplier;

    private int chunkSize = 10;
    private String name;
    private Boolean alwaysReadFromZero;

    public QuerydslPagingItemReaderBuilder<T> entityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        return this;
    }

    public QuerydslPagingItemReaderBuilder<T> querySupplier(Function<JPAQueryFactory, JPAQuery<T>> querySupplier) {
        this.querySupplier = querySupplier;
        return this;
    }

    public QuerydslPagingItemReaderBuilder<T> chunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    public QuerydslPagingItemReaderBuilder<T> name(String name) {
        this.name = name;
        return this;
    }

    public QuerydslPagingItemReaderBuilder<T> alwaysReadFromZero(Boolean alwaysReadFromZero) {
        this.alwaysReadFromZero = alwaysReadFromZero;
        return this;
    }

    public QuerydslPagingItemReader<T> build() {
        if (name == null) {
            this.name = ClassUtils.getShortName(QuerydslPagingItemReader.class);
        }
        if (this.entityManagerFactory == null) {
            throw new IllegalArgumentException("EntityManagerFactory can not be null.!");
        }
        if (this.querySupplier == null) {
            throw new IllegalArgumentException("Function<JPAQueryFactory, JPAQuery<T>> can not be null.!");
        }
        if (this.alwaysReadFromZero == null) {
            alwaysReadFromZero = false;
        }
        
        return new QuerydslPagingItemReader<>(this.name,entityManagerFactory,querySupplier,chunkSize,alwaysReadFromZero);
        
    }
}
```


## 소스 샘플