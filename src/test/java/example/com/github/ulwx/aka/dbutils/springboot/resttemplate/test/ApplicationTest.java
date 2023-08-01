package example.com.github.ulwx.aka.dbutils.springboot.resttemplate.test;


import com.github.ulwx.aka.dbutils.springboot.resttemplate.AkaRestTemplateUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private AkaRestTemplateUtils restTemplateUtils;


    @Test
    public void test(){
        String url="https://www.baidu.com/";
        ResponseEntity<String> ret=restTemplateUtils.get(url,String.class);
        String body=ret.getBody();
        System.out.println(body);
        restTemplateUtils.setTimeout(1000);
        restTemplateUtils.get(url,String.class);


//        url="http://www.baidu.com/";
//        restTemplateUtils.setServiceRequest(true);
//        ret=restTemplateUtils.get(url,String.class);
//        body=ret.getBody();
//        System.out.println(body);
    }




}
