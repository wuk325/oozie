/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oozie.action.hadoop;

import org.apache.hadoop.conf.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JavaMain extends LauncherMain {
    public static final String JAVA_MAIN_CLASS = "oozie.action.java.main";

   /**
    * @param args Invoked from LauncherAM:run()
    * @throws Exception in case of error when running the application
    */
    public static void main(String[] args) throws Exception {
        run(JavaMain.class, args);
    }

    @Override
    protected void run(String[] args) throws Exception {

        Configuration actionConf = loadActionConf();

        setYarnTag(actionConf);
        setApplicationTags(actionConf, TEZ_APPLICATION_TAGS);
        setApplicationTags(actionConf, SPARK_YARN_TAGS);

        LauncherMain.killChildYarnJobs(actionConf);

        Class<?> klass = actionConf.getClass(JAVA_MAIN_CLASS, Object.class);
        System.out.println("Java action main class        : " + klass.getName());
        printArgs("Java action arguments         :", args);
        System.out.println();
        Method mainMethod = klass.getMethod("main", String[].class);
        try {
            mainMethod.invoke(null, (Object) args);
        } catch(InvocationTargetException ex) {
            // Get rid of the InvocationTargetException and wrap the Throwable
            throw new JavaMainException(ex.getCause());
        }
    }


    /**
     * Used by JavaMain to wrap a Throwable when an Exception occurs
     */
    @SuppressWarnings("serial")
    static class JavaMainException extends Exception {
        public JavaMainException(Throwable t) {
            super(t);
        }
    }
}
