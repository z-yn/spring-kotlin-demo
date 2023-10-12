package com.example.kotlinlang

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class KotlinLangTest {
    //============================= 变量 ===========================
    //1. Kotlin使用关键字`val`定义常量,相当于Java中的`final`,
    //2. 使用关键字`var`定义变量，
    //3. 类型可以自动推导(推导为非空类型)
    //4. Kotlin是空安全的,默认是非空的。如果可以为空则需要使用`?`来标注类型。

    @Test
    fun testVariable() {
        val inferedString = "ktolin"
        println("inferred type is ${inferedString::class.java.name}")
        val valueVars: String = "kotlin"
        var variableVars: String = "kotlin"
        var inferedValueString = "kotlin"
        //inferedValueString = null  //错误，var猜测的是非空的
        var nullableValue: String? = null
    }

    //======================== 空安全 ======================

    //空安全是劝退入坑Kotlin的主要原因。
    //需要清楚的指定每个字段是不是可以为空。如果为空调用时需要进行空判断。否则拒绝编译
    //1. 使用`a?.b()`调用可以安全的调用,相当于
    //    ```
    //        if(a!=null) a.b()
    //    ```
    //2. 使用`a ?: b`调用可以安全的调用,相当于
    //    ```
    //        if(a!=null) a else b
    //    ```

    @Test
    fun testNullSafe() {
        val randomValue = Math.random()
        println("randomValue is $randomValue")
        val a: String? = if (randomValue > 0.5) "OK" else null
        println("a = $a")
        a?.let {
            println("not null")
        }
        val b = a ?: "A IS NULL"
        println("b  = $b")
    }

    data class A(val valField: String? = null, var varField: String? = null)

    //空安全检查是强制，
    @Test
    fun testNullSafeCheck() {
        val a = A("val", "var")
        if (a.valField != null) {
            println("length of valField ${a.valField.length}")
        }
        if (a.varField != null) {
            println("length of valField ${a.varField}")
        }
    }


    // ====================== 流程控制 =====================
    //if-else
    //`when`语句替代了`switch`
    //while ,do-while保留
    //三元运算符移除
    //都是when 和 if-else都是语句
    sealed interface Type
    class TypeString(val string: String) : Type
    class TypeInt(val int: Int) : Type

    private fun match(type: Type) = when (type) {
        is TypeString -> println("Got string value: ${type.string}")
        is TypeInt -> println("Got int value:${type.int}")
    }

    private fun printValue(type: Type) = if (type is TypeString) {
        println("Got string value: ${type.string}")
    } else if (type is TypeInt) {
        println("Got int value:${type.int}")
    } else {
        println("should not matched")
    }

    @Test
    fun testWhen() {
        val int = TypeInt(1)
        val string = TypeString("hello")
        match(int);
        match(string)

        printValue(int)
        printValue(string)
    }


    //======================= 函数 ==================

    //1. 函数使用fun定义。 函数支持参数默认值，支持具名传参,
    // 具名参数的一个好处就是可变参数不需要作为函数最后一个参数了，传参使用具名参数便不具有歧义。
    //2. Kotlin中函数也是第一公民，函数也是一种类型。使用`::funName`可以获取该函数的实例,函数实例可以作为函数的传参
    //3. 函数可以有接收者(receiver)，接收者是一种语法糖。但是看起来提供了*扩展函数*的功能
    //4. 函数的接收者使得lambada表达式可以有*上下文*。这使得DSL可以实现
    //5. lambda作为最后一个函数参数可以放到函数调用外用`{}`编写,且此时如果函数没有参数则可以省略()

    fun testFunc() {
        fun addInt(a: Int, b: Int): Int = a + b
        //可以使用具名参数
        println("1 + 2 = ${addInt(a = 1, b = 2)}")

        val addFun = ::addInt
        println("class of addFun is ${addFun::class}")
        addFun.invoke(1, 2)
        addFun(1, 2) //语法糖 相当于 addFun.invoke(1, 2)

        //带receiver的函数。编译的JVM签名是 add(a:Int,b:Int)
        fun Int.add(b: Int): Int = this + b
        println("1.add(2) = ${1.add(2)}")

        //参数默认值是编译实现的，并不同于函数重载，如果希望Java代码中调用需要使用注解@JvmOverload
        fun increase(a: Int, toAdd: Int = 1): Int = a + toAdd
        println("1 increase 1 is ${increase(1)}")


        //接收器的函数作为参数，可以实现DSL效果的构造器能力。
        fun buildString(buildFunc: StringBuilder.() -> Unit): String {
            val stringBuilder = StringBuilder()
            stringBuilder.buildFunc()
            return stringBuilder.toString()
        }

        val string = buildString {
            append("Hello ")
            append(2)
            append("Kotlin ")
            append("!!")
        }
        println("buildString: $string")
    }

    //========================== 类定义 ===============================

    //移除new关键字，构造函数跟普通函数看起来一样，只是函数会返回对象
    //主构造函数内声明字段。
    class Simple(var field1:String,arg1:String){
        //声明其他field
        private var field2:String = arg1

        init {
            //主构造函数的函数体
            println("new Simple")

            field2 = arg1;
        }

        constructor(field1: String) : this(field1,"DefaultValueForArg1") {

        }

    }

    //数据类 data class => lombok @Data
    data class User(val name:String)

    @Test
    fun testDataClass() {
        //dataclass支持析构
        val (name)=User("1");
        Assertions.assertEquals(name,"1")
    }

    //单例对象类
    object Single {}

    @Test
    fun testObjectClass() {
        val a = Single;
        assert(a is Single);
    }


}