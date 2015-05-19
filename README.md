# next-bind 0.0.1

##GET
pom.xml에 아래의 레파지토리와 Dependency설정을 추가합니다.

###Repository
    <repository>
        <id>next-mvn-repo</id>
        <url>https://raw.github.com/zerohouse/next/mvn-repo/</url>
    </repository>

###Dependency
    <dependency>
		<groupId>at.begin</groupId>
		<artifactId>next-bind</artifactId>
		<version>0.0.1</version>
	</dependency>

## How to Use


### 1. Produces

    public class bindFactory {
        @Produces("someId")
        public DAO makeDAO(){
            return new DAO();
        }
    }
    
### 2. Bind

    public class AnswerRouter {
        @Bind("someId")
    	DAO dao;
    }
    
### 3. Instance Pool
    InstancePool instancePool = new InstancePool(basePackage);
    instancePool.addClassAnnotations(SomeAnnotations...);
    instancePool.addFieldAnnotations(SomeAnnotations...);
    instancePool.addMethodAnnotations(SomeAnnotations...);
    instancePool.addClasses(SomeClasses...);
    instancePool.build();
    instancePool.getInstance(Class);
    instancePool.getInstance(field);
    instancePool.getInstance(Method);
    instancePool.getInstance(id);
    instancePool.getInstancesAnnotatedWith(Annotation);
    
    
### 3. ID가 있을경우 ID로 매칭, ID가 없을경우 Class로 매칭.
### 4. Produces하지 않았을 경우 EmptyInstance Bind
