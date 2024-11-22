package org.example;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import javax.xml.stream.XMLInputFactory;
import java.util.concurrent.ExecutionException;

public class App {

    static final String CLASS_NAME = "com.example.CustomInputFactory";
    /**
     * Update credentials in build.gradle
     */
    public static void main(String[] args) throws Exception {
        Thread.currentThread().setContextClassLoader(new CustomClassLoader(CustomClassLoader.class.getClassLoader()));
        //Try with default XMLInputFactory
        aws();
        //Try with an XMLInputFactory that is defined in a child classloader
        System.setProperty("javax.xml.stream.XMLInputFactory", CLASS_NAME);
        aws();
    }

    private static void aws() throws InterruptedException, ExecutionException {
        System.out.println("Trying with XMLInputFactory:" + XMLInputFactory.newInstance().getClass().getName());
        S3AsyncClient.crtBuilder().build().listObjectsV2(ListObjectsV2Request.builder().bucket(System.getProperty("aws.bucket")).build()).get().contents().forEach(S3Object::key);
        System.out.println("Finished with XMLInputFactory:" + XMLInputFactory.newInstance().getClass().getName());
    }


    static class CustomClassLoader extends ClassLoader {
        CustomClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass == null) {
                if (name.equals(CLASS_NAME)) {
                    return new ByteBuddy().subclass(Class.forName("com.sun.xml.internal.stream.XMLInputFactoryImpl")).name(CLASS_NAME).make().load(this, ClassLoadingStrategy.Default.WRAPPER).getLoaded();
                }
                loadedClass = getParent().loadClass(name);
            }
            return loadedClass;
        }
    }
}

