package fun;

import org.apache.commons.lang3.SerializationUtils;

import java.util.Arrays;

public class MainDriver {

    public static void main(String[] args) {


        Pojo p = new Pojo();
        Pojo2 p2 = new Pojo2();

        System.out.println(p2);

        byte[] data = SerializationUtils.serialize(p2);

        System.out.println(Arrays.toString(data));

        Pojo p3 = SerializationUtils.deserialize(data);

        System.out.println(p3);

    }
}
