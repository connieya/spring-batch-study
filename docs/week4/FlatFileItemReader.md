# FlatFileItemReader 로 단순 파일 읽고, FlatFileItemWriter 로 파일에 쓰기

## FlatFileItemReader 개요

- FlatFileItemReader 는 SpringBatch 에서 제공하는 기본적인 ItemReader 로 , 텍스트 파일로부터 데이터를 읽는다.
- 고정 길이, 구분자 기반, 멀티라인 등 다양한 형식의 텍스트 파일을 지원하며, 다음과 같은 장점을 가진다.
- 간단하고 효율적인 구현 : 설정 및 사용이 간편하며, 대규모 데이터 처리에도 효율적이다.
- 다양한 텍스트 파일 형식 지원 : 고정 길이, 구분자 기반, 멀티라인 등 다양한 형식의 텍스트 파일을 읽을 수 있다.
- 확장 가능성 : 토크나이저, 필터 등을 통해 기능을 확장할 수 있다.
- 사용처 : 고정 길이 , 구분자 기반, 멀티라인 등 다양한 형식의 텍스트 파일 데이터 처리 

### 장점 : 간단하고 효율적인 구현, 다양한 텍스트 파일 형식 지원


### 단점 : 복잡한 데이터 구조 처리에는 적합하지 않음


## FlatFileItemReader 주요 구성 요소

- Resource : 읽을 텍스트 파일을 지정한다.
- LineMapper : 텍스프 파일의 각 라인을 Item으로 변환하는 역할을 한다.
- LineTokenizer : 텍스트 파일의 각 라인을 토큰으로 분리하는 역할을 한다.
- FieldSetMapper : 토큰을 Item 의 속성에 매핑하는 역할을 한다.
- SkippableLineMapper : 오류 발생 시 해당 라인을 건너뛸 수 있도록 한다.
- ReadListener : 읽기 시작, 종료, 오류 발생 등의 이벤트를 처리할 수 있도로 한다.

## 샘플코드

### Customer 모델 생성하기

```java
@Getter
@Setter
public class Customer {
    
    private String name;
    private int age;
    private String gender;
}

```

- 읽어들인 정보를 Customer 객체에 매핑할 수 있도록 객체를 정의한다.


### FlatFileItemReader 빈 생성

- FlatFileItemReader 를 생성하고, Customer 객체에 등록하여 반환한다.

```java
public class FlatFileItemJobConfig {
    
    @Bean
    public FlatFileItemReader<Customer> fileFileItemReader() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("FlatFileItemReader")
                .resource(new ClassPathResource("./customers.csv"))
                .encoding(ENCODING)
                .delimited().delimiter(",")
                .names("name","age","gender")
                .targetType(Customer.class)
                .build();
    }
}
```

- resource
  - ClassPathResource("./customer.csv")
  - 클래스 패스 내부에 존재하는 csv 파일을 읽어들인다.
- encoding
  - 파일 데이터의 인코딩을 추가한다.
- delimited
  - 구분자로 설정되어 있음을 의미한다.
- delimiter
  - 구분자를 무엇으로 할지 지정한다.
- names
  - 구분자로 구분된 데이터의 이름을 지정한다.
- targetType
  - 구분된 데이터를 어느 모델에 넣을지 클래스 타입을 지정한다.


## FlatFileItemWriter 개요

- FlatFileItemWriter 는 Spring Batch 에서 제공하는 ItemWriter 인터페이스를 구현한 클래스이다.
- 데이터를 텍스트 파일로 출력하는 데 사용된다.


## FlatFileItemWriter 구성 요소

- Resource : 출력 파일 경로를 지정한다.
- LineAggregator : Item 을 문자열로 변환하는 역할을 한다.
- HeaderCallback : 출력 파일 헤더를 작성하는 역할을 한다.
- FooterCallback : 출력 파일 푸터를 작성하는 역할을 한다. 
- Delimiter : 항목 사이 구분자를 지정한다.
- AppendMode :기존 파일에 추가할지 여부를 지정한다.

### 장점

- 간편성 : 텍스트 파일로 데이터를 출력하는 간편한 방법을 제공한다. 
- 유연성 : 다양한 설정을 통해 원하는 형식으로 출력 파일을 만들 수 있다.
- 성능 : 대량의 데이터를 빠르게 출력할 수 있다.

### 단점

- 형식 제약 : 텍스트 파일 형식만 지원한다.
- 복잡한 구조 : 복잡한 구조의 데이터를 출력할 경우 설정이 복잡해질 수 있다.
- 오류 가능성 : 설정 오류 시 출력 파일이 손상될 수 있다.


### FlatFileItemWriter 빈 생성

````java
@Bean
public FlatFileItemWriter<Customer> flatFileItemWriter() {
  return new FlatFileItemWriterBuilder<Customer>()
          .name("flatFileItemWriter")
          .resource(new FileSystemResource("./output/customer_new.csv"))
          .encoding(ENCODING)
          .delimited().delimiter("\t")
          .names("Name","Age","Gender")
          .append(false)
          .lineAggregator(new CustomerLineAggregator())
          .headerCallback(new CustomerHeader())
          .footerCallback(new CustomerFooter(aggregateInfos))
          .build();
}
````

- FlatFileItemWriterBuilder : 파일로 결과를 쓰게하기 위한 빌더 객체
- name : FlatFileItemWriter 의 이름을 지정한다.
- resource : 저장할 최종 파일 이름
- encoding : 저장할 파일의 인코딩 타입
- delimited().delimiter : 각 필드를 구분할 딜리미터를 지정한다. 
- append : true 인 경우 기존 파일에 첨부한다. false 인 경우 새로운 파일을 만든다.
- lineAggregator : 라인 구분자를 지정한다.
- headerCallback : 출력 파일의 헤더를 지정할 수 있도록 한다.
- footerCallback : 출력 파일의 푸터를 지정할 수 있도록 한다.

### CustomerLineAggregator 작성하기

```java
public class CustomerLineAggregator implements LineAggregator<Customer> {
    @Override
    public String aggregate(Customer item) {
        return item.getName() + "," + item.getAge();
    }
}
```

- LieAggregator 는 FlatFile에 저장할 아이템들을 스트링으로 변환하는 방법을 지정하는 것이다.
- 위와 같이 aggregate 를 구현하여 아이템을 스트링 문자로 변경하는 것을 확인할 수 있다.

### CustomerHeader 작성하기

```java
public class CustomerHeader implements FlatFileHeaderCallback {
    @Override
    public void writeHeader(Writer writer) throws IOException {
        writer.write("ID,AGE");
    }
}

```



## 샘플코드 전체 소스


