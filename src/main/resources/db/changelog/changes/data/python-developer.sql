INSERT INTO courses (name, description, created_time, updated_time)
VALUES ('Python Developer',
        'A practical course for those who want to start programming with Python and grow into backend development, automation or data analysis. Students learn the syntax of the language, built-in data types, conditional logic, loops and functions, and practise writing clean, readable code that follows PEP 8. The course prepares the ground for web frameworks such as Django and FastAPI.',
        NOW(), NOW());

INSERT INTO chapters (name, description, order_number, course_id, created_time, updated_time)
VALUES ('Getting Started with Python',
        'Installing Python, working with the interactive interpreter, running scripts and understanding how indentation defines code blocks.',
        1, (SELECT id FROM courses WHERE name = 'Python Developer'), NOW(), NOW()),
       ('Variables, Types and Operators',
        'Dynamic typing, numbers, strings, booleans, None, type conversion and the most important operators.',
        2, (SELECT id FROM courses WHERE name = 'Python Developer'), NOW(), NOW()),
       ('Conditional Statements: if, elif, else',
        'Branching logic in Python, truthy and falsy values, chained comparisons and the match statement.',
        3, (SELECT id FROM courses WHERE name = 'Python Developer'), NOW(), NOW()),
       ('Functions',
        'Defining functions, positional and keyword arguments, default values, return values, scope and docstrings.',
        4, (SELECT id FROM courses WHERE name = 'Python Developer'), NOW(), NOW());

INSERT INTO lessons (name, description, content, order_number, chapter_id, created_time, updated_time)
VALUES ('Lecture for Getting Started with Python',
        'Theory: what Python is, how the interpreter works and how a Python script is structured.',
        '<h2>Why Python?</h2>
<p>Python is a high-level, interpreted, dynamically typed language created by Guido van Rossum and first released in 1991. Its design philosophy emphasizes readability: the code looks almost like plain English, and there is usually one obvious way to solve a problem. Python is used for web backends, automation scripts, data analysis, machine learning, DevOps tooling and teaching programming.</p>
<h3>The interpreter</h3>
<p>Unlike Java, Python does not require a separate compilation step that you run manually. The interpreter <code>python</code> reads a <code>.py</code> file, compiles it into bytecode in memory and executes it immediately. If you run <code>python</code> without arguments, you get an interactive shell (REPL) where every expression is evaluated as soon as you press Enter. It is the perfect place to experiment.</p>
<h3>The first script</h3>
<pre><code>name = input("What is your name? ")
print(f"Hello, {name}! Welcome to the Python Developer course.")</code></pre>
<p>The function <code>input</code> reads a line from the keyboard and <code>print</code> outputs text. The prefix <code>f</code> creates an f-string, where expressions inside curly braces are replaced with their values.</p>
<h3>Indentation is syntax</h3>
<p>Python does not use curly braces to group statements. A block of code is defined by indentation, and the standard is four spaces. A colon at the end of a line opens a new block:</p>
<pre><code>if name:
    print("Nice to meet you")
print("This line is outside the block")</code></pre>
<p>Mixing tabs and spaces or using inconsistent indentation leads to an <code>IndentationError</code>. Configure your editor to insert four spaces when you press Tab.</p>
<h3>Virtual environments and packages</h3>
<p>Real projects depend on third-party packages installed with <code>pip</code>. To keep dependencies of different projects separate, create a virtual environment with <code>python -m venv .venv</code> and activate it before installing packages. The list of dependencies is usually stored in <code>requirements.txt</code>.</p>
<h3>Style</h3>
<p>The official style guide PEP 8 describes naming and formatting rules: <code>snake_case</code> for variables and functions, <code>PascalCase</code> for classes, lines no longer than 79 characters. Tools such as <code>black</code> and <code>ruff</code> apply these rules automatically.</p>',
        1, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Python Developer' AND ch.order_number = 1), NOW(), NOW()),
       ('Practice for Getting Started with Python',
        'Hands-on: install Python, use the REPL, write and run scripts in a virtual environment.',
        '<h2>Practice: setting up Python</h2>
<h3>Task 1. Installation</h3>
<p>Install Python 3.12 or newer from python.org. On Windows, tick the option that adds Python to PATH. Run <code>python --version</code> and <code>pip --version</code> in a terminal and make sure both commands work.</p>
<h3>Task 2. The REPL as a calculator</h3>
<p>Start the interactive shell and calculate: the number of seconds in a week, <code>2 ** 100</code>, the result of <code>17 / 5</code> and of <code>17 // 5</code>. Note that Python integers have no fixed size, so <code>2 ** 100</code> is printed exactly.</p>
<h3>Task 3. A greeting script</h3>
<p>Create the file <code>greeting.py</code> that asks for the name and the city of the user and prints a two-line greeting using f-strings. Run it with <code>python greeting.py</code>.</p>
<h3>Task 4. Virtual environment</h3>
<p>Create a folder <code>python-course</code>, create a virtual environment inside it, activate it and install the package <code>requests</code>. Write a script that prints the status code of a request to <code>https://www.python.org</code>. Save the dependencies with <code>pip freeze &gt; requirements.txt</code>.</p>
<h3>Task 5. Fix the indentation</h3>
<pre><code>age = int(input("Age: "))
if age &gt;= 18:
print("Access granted")
  print("Welcome")</code></pre>
<p>Run the code, read the error message carefully and fix the indentation so that both messages are printed only for adults.</p>',
        2, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Python Developer' AND ch.order_number = 1), NOW(), NOW()),

       ('Lecture for Variables, Types and Operators',
        'Theory: dynamic typing, built-in scalar types, strings and operators.',
        '<h2>Variables and dynamic typing</h2>
<p>In Python a variable is a name that refers to an object. You do not declare the type: it is determined by the value at run time, and the same name can later refer to an object of another type. This is called dynamic typing.</p>
<pre><code>students = 25          # int
average = 87.5         # float
course = "Python"      # str
is_open = True         # bool
mentor = None          # NoneType</code></pre>
<p>Python is dynamically but strongly typed: it never silently converts a string into a number. The expression <code>"5" + 3</code> raises a <code>TypeError</code>. Convert values explicitly with <code>int()</code>, <code>float()</code>, <code>str()</code> and <code>bool()</code>.</p>
<h3>Numbers</h3>
<p>Integers have arbitrary precision. The operator <code>/</code> always returns a float, <code>//</code> performs floor division, <code>%</code> returns the remainder and <code>**</code> raises to a power. For money use the <code>decimal</code> module, because floats cannot represent most decimal fractions exactly: <code>0.1 + 0.2</code> is not equal to <code>0.3</code>.</p>
<h3>Strings</h3>
<p>Strings can be written in single or double quotes and are immutable. They support indexing <code>text[0]</code>, slicing <code>text[1:4]</code>, and many useful methods: <code>upper()</code>, <code>strip()</code>, <code>split()</code>, <code>replace()</code>, <code>startswith()</code>. f-strings are the preferred way to build text from values: <code>f"{name} scored {score:.1f}"</code>.</p>
<h3>Booleans and None</h3>
<p><code>True</code> and <code>False</code> are the boolean values. <code>None</code> represents the absence of a value and is checked with <code>is None</code>, not with <code>==</code>.</p>
<h3>Operators</h3>
<ul>
  <li>Comparison: <code>== != &lt; &gt; &lt;= &gt;=</code>; comparisons can be chained: <code>0 &lt;= score &lt;= 100</code>.</li>
  <li>Logical: <code>and</code>, <code>or</code>, <code>not</code>.</li>
  <li>Membership: <code>in</code> and <code>not in</code>, for example <code>"py" in "python"</code>.</li>
  <li>Augmented assignment: <code>+=</code>, <code>-=</code>, <code>*=</code> and others.</li>
</ul>',
        1, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Python Developer' AND ch.order_number = 2), NOW(), NOW()),
       ('Practice for Variables, Types and Operators',
        'Hands-on: conversions, string processing and calculations.',
        '<h2>Practice: types and operators</h2>
<h3>Task 1. Shopping receipt</h3>
<p>Ask the user for the name of a product, its price and the quantity. Convert the input to the right types and print a receipt line in the format <code>Laptop x 2 = 900000.00 KZT</code> using an f-string with two decimal places.</p>
<h3>Task 2. Type experiments</h3>
<p>Predict the result of each expression, then check it in the REPL: <code>7 / 2</code>, <code>7 // 2</code>, <code>-7 // 2</code>, <code>7 % 3</code>, <code>2 ** 10</code>, <code>int("42") + 8</code>, <code>str(42) + "8"</code>, <code>bool("")</code>, <code>bool("False")</code>. Write down every case where your prediction was wrong and explain why.</p>
<h3>Task 3. Working with strings</h3>
<p>Given <code>full_name = "  aigerim nurlanovna sadykova  "</code>, remove the extra spaces, capitalize every word, and print the initials in the form <code>A. N. S.</code>. Use only string methods and slicing.</p>
<h3>Task 4. Time converter</h3>
<p>Read a number of seconds and print it as hours, minutes and seconds using <code>//</code> and <code>%</code>. Then solve the task again with the built-in function <code>divmod</code>.</p>
<h3>Task 5. Floating point precision</h3>
<pre><code>from decimal import Decimal

print(0.1 + 0.2 == 0.3)
print(Decimal("0.1") + Decimal("0.2") == Decimal("0.3"))</code></pre>
<p>Run the code and explain the difference between the two results. In which kinds of applications is this difference critical?</p>',
        2, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Python Developer' AND ch.order_number = 2), NOW(), NOW()),

       ('Lecture for Conditional Statements: if, elif, else',
        'Theory: branching, truthiness, conditional expressions and structural pattern matching.',
        '<h2>Branching in Python</h2>
<p>The <code>if</code> statement runs a block only when its condition is true. Additional branches are added with <code>elif</code>, and <code>else</code> handles every remaining case. Only the first branch whose condition is true is executed.</p>
<pre><code>score = int(input("Score: "))

if score &gt;= 90:
    grade = "A"
elif score &gt;= 75:
    grade = "B"
elif score &gt;= 50:
    grade = "C"
else:
    grade = "F"

print(f"Your grade is {grade}")</code></pre>
<h3>Truthy and falsy values</h3>
<p>Any object can be used as a condition. The values <code>False</code>, <code>None</code>, <code>0</code>, <code>0.0</code>, an empty string, an empty list, an empty dictionary and an empty set are considered false. Everything else is true. This allows short and idiomatic checks: <code>if students:</code> means the list is not empty.</p>
<h3>Combining conditions</h3>
<p>Use <code>and</code>, <code>or</code> and <code>not</code>. Python evaluates them lazily, and chained comparisons such as <code>18 &lt;= age &lt; 65</code> make range checks very readable.</p>
<h3>Conditional expression</h3>
<p>A one-line form is available for simple choices: <code>status = "adult" if age &gt;= 18 else "minor"</code>.</p>
<h3>The match statement</h3>
<p>Since Python 3.10 the <code>match</code> statement compares a value against patterns. It is more powerful than a classic switch because it can also destructure sequences and objects:</p>
<pre><code>match command.split():
    case ["start"]:
        print("Starting the course")
    case ["open", lesson]:
        print(f"Opening lesson {lesson}")
    case _:
        print("Unknown command")</code></pre>
<h3>Good practices</h3>
<ul>
  <li>Prefer early returns over deeply nested <code>if</code> blocks.</li>
  <li>Check for <code>None</code> with <code>is None</code>.</li>
  <li>Keep conditions short; move complex logic into a well-named function such as <code>is_eligible_for_discount(student)</code>.</li>
</ul>',
        1, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Python Developer' AND ch.order_number = 3), NOW(), NOW()),
       ('Practice for Conditional Statements: if, elif, else',
        'Hands-on: validation rules, calculators and command parsing.',
        '<h2>Practice: conditions</h2>
<h3>Task 1. Password strength</h3>
<p>Read a password and classify it: <code>weak</code> if it is shorter than 8 characters, <code>medium</code> if it is long enough but contains only letters or only digits, and <code>strong</code> if it is at least 8 characters long and contains both letters and digits. Use the string methods <code>isalpha()</code> and <code>isdigit()</code> and the function <code>any()</code>.</p>
<h3>Task 2. Simple calculator</h3>
<p>Read two numbers and an operator (<code>+</code>, <code>-</code>, <code>*</code>, <code>/</code>). Print the result. Division by zero must produce a friendly message instead of an exception, and an unknown operator must be reported.</p>
<h3>Task 3. Ticket price</h3>
<p>A museum ticket costs 3000 tenge. Children under 7 enter for free, students and pensioners pay half price, and on Wednesdays everybody gets an additional 10 percent discount. Read the age, the category and the day of the week and print the final price.</p>
<h3>Task 4. Triangle classifier</h3>
<p>Read three side lengths. First check that such a triangle exists (each side is less than the sum of the other two). Then print whether it is equilateral, isosceles or scalene.</p>
<h3>Task 5. Command parser</h3>
<p>Using the <code>match</code> statement, write a loop that reads commands: <code>list</code> prints the courses, <code>open N</code> prints <code>Opening lesson N</code>, <code>quit</code> stops the program, and anything else prints a hint with available commands.</p>',
        2, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Python Developer' AND ch.order_number = 3), NOW(), NOW()),

       ('Lecture for Functions',
        'Theory: defining and calling functions, arguments, return values and scope.',
        '<h2>Functions in Python</h2>
<p>A function is a named block of code that performs one task and can be reused. Functions make programs shorter, easier to test and easier to read. A function is defined with the keyword <code>def</code>:</p>
<pre><code>def average(grades):
    """Return the arithmetic mean of a non-empty list of grades."""
    return sum(grades) / len(grades)


result = average([84, 91, 67])</code></pre>
<p>The string right after the header is a docstring. It documents what the function does and is shown by <code>help(average)</code> and by IDE tooltips.</p>
<h3>Arguments</h3>
<ul>
  <li><strong>Positional</strong> arguments are matched by order: <code>enroll("Aigerim", "Python")</code>.</li>
  <li><strong>Keyword</strong> arguments are matched by name: <code>enroll(course="Python", student="Aigerim")</code>.</li>
  <li><strong>Default values</strong> make parameters optional: <code>def enroll(student, course, group=1)</code>. Never use a mutable object such as a list as a default value; use <code>None</code> and create the list inside the function.</li>
  <li><code>*args</code> collects extra positional arguments into a tuple, and <code>**kwargs</code> collects extra keyword arguments into a dictionary.</li>
</ul>
<h3>Return values</h3>
<p>The <code>return</code> statement ends the function and passes a value back to the caller. A function without <code>return</code> returns <code>None</code>. Several values can be returned as a tuple: <code>return minimum, maximum</code>.</p>
<h3>Scope</h3>
<p>Variables created inside a function are local and disappear after the call. A function can read global variables, but should not modify them: pass data in through parameters and return results instead. This keeps functions predictable and easy to test.</p>
<h3>Type hints</h3>
<p>Annotations document the expected types and help tools find mistakes: <code>def average(grades: list[float]) -&gt; float:</code>. Python does not enforce them at run time, but checkers such as <code>mypy</code> do.</p>',
        1, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Python Developer' AND ch.order_number = 4), NOW(), NOW()),
       ('Practice for Functions',
        'Hands-on: write reusable functions with arguments, defaults and type hints.',
        '<h2>Practice: functions</h2>
<h3>Task 1. Grade utilities</h3>
<p>Write three functions with type hints and docstrings: <code>average(grades)</code>, <code>best(grades)</code> and <code>letter(score)</code>, which converts a score into a letter grade. Use them together to print a report for a list of scores.</p>
<h3>Task 2. Default arguments</h3>
<p>Write <code>format_price(amount, currency="KZT", decimals=2)</code> that returns a string such as <code>150000.00 KZT</code>. Call it with positional arguments, with keyword arguments and with the defaults.</p>
<h3>Task 3. The mutable default trap</h3>
<pre><code>def add_student(name, group=[]):
    group.append(name)
    return group

print(add_student("Aigerim"))
print(add_student("Daniyar"))</code></pre>
<p>Run the code and explain the output. Then fix the function by using <code>None</code> as the default value.</p>
<h3>Task 4. Flexible sum</h3>
<p>Write <code>total(*numbers)</code> that accepts any number of arguments and returns their sum, ignoring values that are not numbers. Test it with <code>total(1, 2, 3)</code>, <code>total()</code> and <code>total(5, "x", 2.5)</code>.</p>
<h3>Task 5. Refactoring</h3>
<p>Take the calculator from the previous chapter and split it into small functions: one to read a number, one to read an operator, one to calculate and one to print the result. The main part of the script must be only a few lines long. Compare the readability of the two versions.</p>',
        2, (SELECT ch.id FROM chapters ch JOIN courses c ON c.id = ch.course_id WHERE c.name = 'Python Developer' AND ch.order_number = 4), NOW(), NOW());
