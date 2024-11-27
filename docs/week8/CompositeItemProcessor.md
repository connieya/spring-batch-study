# CompositeItemProcessor 으로 여러 단계에 걸쳐 데이터 Transform 하기

## CompositeItemProcessor 개요

- CompositeItemProcessor 는 Spring Batch 에서 제공하는 ItemProcessor 인터페이스를 구현하는 클래스이다.
- 여러 개의 ItemProcessor 를 하나의 Processor 로 연결하여 여러 단계의 처리를 수행할 수 있도록 한다.

## 주요 구성 요소

- Delegates : 처리를 수행할 ItemProcessor 목록이다.
- TransactionAttribute : 트랜잭션 속성을 설정한다.

### 장점

- 단계별 처리 : 여러 단계로 나누어 처리를 수행하여 코드를 명확하고 이해하기 쉽게 만들 수 있다.
- 재사용 가능성 : 각 단계별 Processor 를 재사용하여 다른 Job 에서도 활용할 수 있다.
- 유연성 : 다양한 ItemProcessor 를 조합하여 원하는 처리 과정을 구현할 수 있다.

### 단점

- 설정 복잡성 : 여러 개의 Processor 를 설정하고 관리해야 하기 때문에 설정이 복잡해질 수 있다.
- 성능 저하 : 여러 단계의 처리 과정을 거치므로 성능이 저하될 수 있다.