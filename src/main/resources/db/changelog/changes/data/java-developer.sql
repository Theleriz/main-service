INSERT INTO courses (name, description, created_time, updated_time)
VALUES ('Java Developer',
        'A complete path from the first line of Java code to confident backend development. The course covers the Java platform and the JVM, the type system, control flow, loops and the foundations of object-oriented programming. Every chapter combines a theoretical lecture with a hands-on practice session, so that students write real code from the very first week and build a solid base for Spring Boot, databases and microservices.',
        NOW(), NOW());

INSERT INTO chapters (name, description, order_number, course_id, created_time, updated_time)
VALUES ('Introduction to Java and the JVM',
        'How Java programs are compiled and executed, what the JDK, JRE and JVM are, and how to write, compile and run the first Java application.',
        1, (SELECT id FROM courses WHERE name = 'Java Developer'), NOW(), NOW()),
       ('Variables and Primitive Data Types',
        'Declaring variables, the eight primitive types, type casting, the String class and the basic arithmetic and comparison operators.',
        2, (SELECT id FROM courses WHERE name = 'Java Developer'), NOW(), NOW()),
       ('if-else conditions',
        'Making decisions in code with if, else if and else, combining conditions with logical operators, the ternary operator and the switch statement.',
        3, (SELECT id FROM courses WHERE name = 'Java Developer'), NOW(), NOW()),
       ('Loops: for, while and do-while',
        'Repeating actions with for, while and do-while loops, controlling iterations with break and continue, and iterating over arrays.',
        4, (SELECT id FROM courses WHERE name = 'Java Developer'), NOW(), NOW());

INSERT INTO lessons (name, description, content, order_number, chapter_id, created_time, updated_time)
VALUES ('Lecture for Introduction to Java and the JVM',
        'Theory: the Java platform, the compilation process and the structure of a Java program.',
        '<h2>What is Java?</h2>
<p>Java is a general-purpose, statically typed, object-oriented programming language created at Sun Microsystems in 1995 and maintained today by Oracle and the OpenJDK community. Its famous motto is <em>"Write once, run anywhere"</em>: a program compiled on Windows runs without changes on Linux or macOS. This portability, together with a huge ecosystem of libraries, made Java one of the most popular languages for enterprise backends, banking systems, Android applications and big data tools.</p>
<h3>JDK, JRE and JVM</h3>
<ul>
  <li><strong>JVM (Java Virtual Machine)</strong> executes compiled bytecode. Each operating system has its own JVM implementation, which is exactly what makes Java portable.</li>
  <li><strong>JRE (Java Runtime Environment)</strong> is the JVM plus the standard class library required to run applications.</li>
  <li><strong>JDK (Java Development Kit)</strong> is the JRE plus developer tools: the compiler <code>javac</code>, the debugger, <code>jshell</code> and others. Developers always install the JDK.</li>
</ul>
<h3>From source code to a running program</h3>
<p>You write source code in files with the <code>.java</code> extension. The compiler <code>javac</code> translates them into <code>.class</code> files that contain bytecode, a platform-independent instruction set. When you start the program with the <code>java</code> command, the JVM loads the classes, verifies the bytecode and executes it. Frequently executed code is compiled into native machine code by the JIT (Just-In-Time) compiler, so long-running Java services are very fast.</p>
<h3>The structure of a Java program</h3>
<pre><code>public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, BITLAB ACADEMY!");
    }
}</code></pre>
<p>Every piece of code in Java lives inside a class. The file name must match the name of the public class: <code>HelloWorld.java</code>. The <code>main</code> method is the entry point of the application; the JVM calls it when the program starts. The statement <code>System.out.println</code> prints a line of text to the console. Every statement ends with a semicolon and blocks of code are enclosed in curly braces.</p>
<h3>Key takeaways</h3>
<ul>
  <li>Java code is compiled into bytecode and executed by the JVM.</li>
  <li>Install the JDK to write programs; the JRE alone is only enough to run them.</li>
  <li>A program starts from the <code>public static void main(String[] args)</code> method.</li>
</ul>',
        1, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Java Developer' AND ch.order_number = 1), NOW(), NOW()),
       ('Practice for Introduction to Java and the JVM',
        'Hands-on: install the JDK, compile and run programs from the command line and from IntelliJ IDEA.',
        '<h2>Practice: your first Java programs</h2>
<p>In this practice session you will set up the development environment and write several small programs. Complete the tasks in order and make sure each program compiles and runs before moving to the next one.</p>
<h3>Task 1. Install the JDK</h3>
<p>Download and install JDK 21 (for example, Eclipse Temurin). Open a terminal and run <code>java -version</code> and <code>javac -version</code>. Both commands must print version 21. If the commands are not found, add the <code>bin</code> folder of the JDK to the <code>PATH</code> environment variable.</p>
<h3>Task 2. Compile and run from the terminal</h3>
<p>Create a file <code>Greeting.java</code> with a class that prints your name, the name of the course and today''s date on three separate lines. Compile it with <code>javac Greeting.java</code>, look at the generated <code>Greeting.class</code> file and run it with <code>java Greeting</code>.</p>
<h3>Task 3. Work in IntelliJ IDEA</h3>
<p>Create a new Maven project in IntelliJ IDEA, add the same class and run it with the green arrow next to the <code>main</code> method. Find the <code>target/classes</code> folder and confirm that IntelliJ also produces bytecode.</p>
<h3>Task 4. Command line arguments</h3>
<pre><code>public class Welcome {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please pass your name as an argument");
            return;
        }
        System.out.println("Welcome to the Java Developer course, " + args[0] + "!");
    }
}</code></pre>
<p>Run the program with and without an argument: <code>java Welcome Aigerim</code>. Explain in your own words where the value of <code>args[0]</code> comes from.</p>
<h3>Self-check questions</h3>
<ol>
  <li>What is the difference between the JDK and the JRE?</li>
  <li>Why can the same <code>.class</code> file run on Windows and Linux?</li>
  <li>What happens if the file name does not match the public class name?</li>
</ol>',
        2, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Java Developer' AND ch.order_number = 1), NOW(), NOW()),

       ('Lecture for Variables and Primitive Data Types',
        'Theory: variables, the eight primitive types, strings, operators and type conversion.',
        '<h2>Variables in Java</h2>
<p>A variable is a named piece of memory that stores a value. Java is a statically typed language: the type of every variable is known at compile time and cannot change later. A variable is declared by specifying its type and name, and it can be initialized immediately:</p>
<pre><code>int studentsCount = 25;
double averageScore = 87.5;
boolean isActive = true;
char grade = ''A'';
String courseName = "Java Developer";</code></pre>
<p>Since Java 10 you can use <code>var</code> for local variables, and the compiler infers the type from the assigned value: <code>var lessons = 12;</code> still creates an <code>int</code>.</p>
<h3>The eight primitive types</h3>
<ul>
  <li><strong>byte</strong> (8 bits), <strong>short</strong> (16 bits), <strong>int</strong> (32 bits) and <strong>long</strong> (64 bits) store whole numbers. <code>int</code> is the default choice; use <code>long</code> for large values such as identifiers or timestamps.</li>
  <li><strong>float</strong> (32 bits) and <strong>double</strong> (64 bits) store fractional numbers. Prefer <code>double</code>, and never use floating point types for money: use <code>BigDecimal</code> instead.</li>
  <li><strong>char</strong> stores a single UTF-16 character in single quotes.</li>
  <li><strong>boolean</strong> stores <code>true</code> or <code>false</code>.</li>
</ul>
<h3>Strings</h3>
<p><code>String</code> is not a primitive but a class. Strings are immutable: methods such as <code>toUpperCase()</code> or <code>replace()</code> return a new string instead of modifying the original. Compare strings with <code>equals()</code>, never with <code>==</code>, because <code>==</code> compares references, not content.</p>
<h3>Operators</h3>
<p>Arithmetic operators are <code>+ - * / %</code>. Integer division discards the fractional part: <code>7 / 2</code> equals <code>3</code>, while <code>7 % 2</code> returns the remainder <code>1</code>. Comparison operators <code>== != &lt; &gt; &lt;= &gt;=</code> return a boolean value.</p>
<h3>Type conversion</h3>
<p>Widening conversion happens automatically, for example from <code>int</code> to <code>long</code> or <code>double</code>. Narrowing conversion requires an explicit cast and may lose data: <code>int rounded = (int) 9.99;</code> stores <code>9</code>. To convert text into a number use <code>Integer.parseInt("42")</code> or <code>Double.parseDouble("3.14")</code>.</p>',
        1, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Java Developer' AND ch.order_number = 2), NOW(), NOW()),
       ('Practice for Variables and Primitive Data Types',
        'Hands-on: declare variables, calculate values and convert between types.',
        '<h2>Practice: working with variables</h2>
<h3>Task 1. Student card</h3>
<p>Declare variables for a student: full name, age, group number, average grade and a flag that shows whether the student has a scholarship. Print all values in a readable format, one per line.</p>
<h3>Task 2. Temperature converter</h3>
<p>Write a program that stores a temperature in degrees Celsius in a <code>double</code> variable and prints it in Fahrenheit and Kelvin. Use the formulas <code>F = C * 9 / 5 + 32</code> and <code>K = C + 273.15</code>. Check your program with the values 0, 36.6 and -40.</p>
<h3>Task 3. Integer division</h3>
<pre><code>int totalMinutes = 135;
int hours = totalMinutes / 60;
int minutes = totalMinutes % 60;
System.out.println(hours + " h " + minutes + " min");</code></pre>
<p>Run the example and then change it so that it converts a number of seconds into hours, minutes and seconds. Test it with 3725 seconds: the expected output is <code>1 h 2 min 5 sec</code>.</p>
<h3>Task 4. Overflow</h3>
<p>Declare <code>int max = Integer.MAX_VALUE;</code> and print <code>max + 1</code>. Explain the result. Then repeat the experiment with a <code>long</code> variable and compare the output.</p>
<h3>Task 5. Parsing input</h3>
<p>Use <code>Scanner</code> to read two numbers as strings, convert them with <code>Integer.parseInt</code> and print their sum, difference, product, quotient and remainder. What happens if the user enters a word instead of a number?</p>',
        2, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Java Developer' AND ch.order_number = 2), NOW(), NOW()),

       ('Lecture for if-else conditions',
        'Theory: conditional statements, logical operators, the ternary operator and switch.',
        '<h2>Making decisions with if-else</h2>
<p>Programs rarely execute every line from top to bottom. Most of the time the code must react to data: show a discount only to premium users, reject a payment if the balance is too low, or grant access to a lesson only after the previous one is completed. The <code>if</code> statement executes a block of code only when a boolean condition is <code>true</code>.</p>
<pre><code>int score = 78;
if (score &gt;= 50) {
    System.out.println("The test is passed");
} else {
    System.out.println("The test is failed, try again");
}</code></pre>
<h3>Chains of conditions</h3>
<p>When there are more than two possible outcomes, use <code>else if</code>. Conditions are checked from top to bottom, and only the first matching branch is executed, so the order of the checks matters:</p>
<pre><code>if (score &gt;= 90) {
    grade = ''A'';
} else if (score &gt;= 75) {
    grade = ''B'';
} else if (score &gt;= 50) {
    grade = ''C'';
} else {
    grade = ''F'';
}</code></pre>
<h3>Logical operators</h3>
<p>Conditions can be combined: <code>&amp;&amp;</code> (AND) is true only when both sides are true, <code>||</code> (OR) is true when at least one side is true, and <code>!</code> (NOT) inverts a value. Java evaluates them lazily: in <code>user != null &amp;&amp; user.isActive()</code> the second part is not executed when the user is <code>null</code>, which protects the program from a <code>NullPointerException</code>.</p>
<h3>The ternary operator</h3>
<p>For short assignments there is a compact form: <code>String status = age &gt;= 18 ? "adult" : "minor";</code>. Use it only for simple expressions; nested ternary operators quickly become unreadable.</p>
<h3>The switch statement</h3>
<p>When a single value is compared with many constants, <code>switch</code> is clearer than a long chain of <code>else if</code>. Modern Java supports the arrow syntax, which does not require <code>break</code> and can return a value:</p>
<pre><code>String dayType = switch (day) {
    case "SATURDAY", "SUNDAY" -&gt; "weekend";
    default -&gt; "working day";
};</code></pre>
<h3>Common mistakes</h3>
<ul>
  <li>Using <code>=</code> (assignment) instead of <code>==</code> (comparison).</li>
  <li>Comparing strings with <code>==</code> instead of <code>equals()</code>.</li>
  <li>Omitting curly braces: without them only the first statement belongs to the <code>if</code>.</li>
</ul>',
        1, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Java Developer' AND ch.order_number = 3), NOW(), NOW()),
       ('Practice for if-else conditions',
        'Hands-on: implement grading, validation and discount rules with conditional statements.',
        '<h2>Practice: conditional logic</h2>
<h3>Task 1. Even or odd</h3>
<p>Read an integer from the console and print whether it is even or odd. Use the remainder operator <code>%</code>. Make sure the program also works for negative numbers and zero.</p>
<h3>Task 2. Exam grade</h3>
<p>Read a score from 0 to 100 and print the letter grade: 90 and above is A, 75 to 89 is B, 50 to 74 is C, below 50 is F. If the score is outside the range from 0 to 100, print an error message instead of a grade.</p>
<h3>Task 3. Leap year</h3>
<p>A year is a leap year if it is divisible by 4 but not by 100, or if it is divisible by 400. Write the condition as a single boolean expression and test it with the years 1900, 2000, 2024 and 2026.</p>
<pre><code>boolean isLeap = (year % 4 == 0 &amp;&amp; year % 100 != 0) || year % 400 == 0;</code></pre>
<h3>Task 4. Course discount</h3>
<p>The academy gives discounts: students under 18 get 30 percent, students who already completed another course get 20 percent, and everybody else pays the full price of 150000 tenge. If a student matches both rules, the larger discount is applied. Print the final price.</p>
<h3>Task 5. Day of the week</h3>
<p>Read a number from 1 to 7 and print the name of the day using a <code>switch</code> expression with the arrow syntax. For any other number print <code>Unknown day</code>.</p>
<h3>Self-check</h3>
<p>For each task, write down at least three input values that cover every branch of your conditions and run the program with them. Testing each branch is a habit that will later turn into writing unit tests.</p>',
        2, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Java Developer' AND ch.order_number = 3), NOW(), NOW()),

       ('Lecture for Loops: for, while and do-while',
        'Theory: the three loop statements, break and continue, nested loops and arrays.',
        '<h2>Repeating actions with loops</h2>
<p>Loops execute a block of code many times. They are used to process every element of a collection, to retry an operation until it succeeds, or to read user input until a valid value is entered. Java has three loop statements.</p>
<h3>The for loop</h3>
<p>Use <code>for</code> when the number of iterations is known in advance. Its header contains three parts: initialization, a condition that is checked before every iteration, and an update that runs after every iteration.</p>
<pre><code>for (int i = 1; i &lt;= 5; i++) {
    System.out.println("Lesson " + i);
}</code></pre>
<h3>The while loop</h3>
<p><code>while</code> checks the condition before each iteration and is suitable when the number of repetitions depends on data:</p>
<pre><code>int balance = 1000;
int months = 0;
while (balance &lt; 2000) {
    balance = balance + balance / 10;
    months++;
}</code></pre>
<h3>The do-while loop</h3>
<p><code>do-while</code> checks the condition after the body, so the body is executed at least once. It is convenient for menus and input validation, where the user must see the prompt at least one time.</p>
<h3>break and continue</h3>
<p><code>break</code> immediately exits the loop, and <code>continue</code> skips the rest of the current iteration and moves to the next one. Use them carefully: too many jumps make the flow of a program hard to follow.</p>
<h3>Loops and arrays</h3>
<p>An array stores a fixed number of values of the same type. The enhanced for loop iterates over every element without an index:</p>
<pre><code>int[] scores = {78, 92, 65, 88};
int sum = 0;
for (int score : scores) {
    sum += score;
}
double average = (double) sum / scores.length;</code></pre>
<h3>Typical pitfalls</h3>
<ul>
  <li>An infinite loop appears when the condition never becomes false, for example when you forget to increment the counter.</li>
  <li>An off-by-one error happens when you use <code>&lt;=</code> instead of <code>&lt;</code> with an array length and get an <code>ArrayIndexOutOfBoundsException</code>.</li>
</ul>',
        1, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Java Developer' AND ch.order_number = 4), NOW(), NOW()),
       ('Practice for Loops: for, while and do-while',
        'Hands-on: sums, tables, number guessing and array processing.',
        '<h2>Practice: loops</h2>
<h3>Task 1. Sum of numbers</h3>
<p>Calculate the sum of all numbers from 1 to N, where N is entered by the user. Solve the task with a <code>for</code> loop, then check the answer with the formula <code>N * (N + 1) / 2</code>.</p>
<h3>Task 2. Multiplication table</h3>
<p>Print the multiplication table from 1 to 9 using two nested <code>for</code> loops. Align the columns with <code>System.out.printf("%4d", value)</code>.</p>
<h3>Task 3. Guess the number</h3>
<p>The program picks a random number from 1 to 100 with <code>new Random().nextInt(100) + 1</code>. The user keeps guessing, and after each attempt the program answers <code>higher</code> or <code>lower</code>. When the number is guessed, print the number of attempts. Choose the loop type that fits this task best and explain your choice.</p>
<h3>Task 4. Array statistics</h3>
<p>Given the array <code>int[] grades = {84, 91, 67, 73, 98, 55, 80};</code> find the minimum, the maximum and the average value in a single pass, and count how many grades are above the average in a second pass.</p>
<h3>Task 5. Prime numbers</h3>
<p>Print all prime numbers from 2 to 100. For every candidate, check divisors only up to its square root and use <code>break</code> as soon as a divisor is found.</p>
<h3>Bonus</h3>
<p>Write a console menu with a <code>do-while</code> loop: 1 shows the list of courses, 2 shows the schedule, 0 exits. The menu must repeat until the user chooses 0.</p>',
        2, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Java Developer' AND ch.order_number = 4), NOW(), NOW());
