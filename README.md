# Introduction
Hello guys, this is my project - **Lox Interpreter**. For someone who don't know what it is. Well, basically it interprets Lox - a programming language. My project supports almost all of basic definitions of programming language, such as variable, expression, class, function, recursion, printing... Unfortunately, I don't have much time right now so I can't add any libraries yet.

This project follows the book **Crafting Interpreters**. I suggest you read it once, and if you have time then give it a try.

# Tutorial
If you want to try my interpreter, clone this respository to your computer. After that, in folder `src/main/java` you can edit file `test.lox` or create new file if you want, let's assume this `test.lox`. In this file, write any Lox-code as you like. In case you don't know any thing about Lox, here is an example:
![image](https://github.com/user-attachments/assets/366514ef-1353-4894-b262-063b8a71ba01)

Then, in this folder, open terminal and enter this 

```sh
java Main.java run test.lox
```
**Note**: As you can see, the most important thing if you want to try is that your computer must have Java environment before. Again, in case you don't know how to install it, you can search for tutorial on Youtube. I can guide you but it will take a lot of time. And, urrghh, I'm lazy ^^.

That's it. I have showed you guys all about my project. If you have any other questions, feel free to ask me.

[Dennis Tran](https://www.facebook.com/DennisTran1402)


This is a starting point for Java solutions to the
["Build your own Interpreter" Challenge](https://app.codecrafters.io/courses/interpreter/overview).

This challenge follows the book
[Crafting Interpreters](https://craftinginterpreters.com/) by Robert Nystrom.

In this challenge you'll build an interpreter for
[Lox](https://craftinginterpreters.com/the-lox-language.html), a simple
scripting language. Along the way, you'll learn about tokenization, ASTs,
tree-walk interpreters and more.

Before starting this challenge, make sure you've read the "Welcome" part of the
book that contains these chapters:

- [Introduction](https://craftinginterpreters.com/introduction.html) (chapter 1)
- [A Map of the Territory](https://craftinginterpreters.com/a-map-of-the-territory.html)
  (chapter 2)
- [The Lox Language](https://craftinginterpreters.com/the-lox-language.html)
  (chapter 3)

These chapters don't involve writing code, so they won't be covered in this
challenge. This challenge will start from chapter 4,
[Scanning](https://craftinginterpreters.com/scanning.html).

**Note**: If you're viewing this repo on GitHub, head over to
[codecrafters.io](https://codecrafters.io) to try the challenge.

# Passing the first stage

The entry point for your program is in `src/main/java/Main.java`. Study and
uncomment the relevant code, and push your changes to pass the first stage:

```sh
git commit -am "pass 1st stage" # any msg
git push origin master
```

Time to move on to the next stage!

# Stage 2 & beyond

Note: This section is for stages 2 and beyond.

1. Ensure you have `mvn` installed locally
2. Run `./your_program.sh` to run your program, which is implemented in
   `src/main/java/Main.java`.
3. Commit your changes and run `git push origin master` to submit your solution
   to CodeCrafters. Test output will be streamed to your terminal.
