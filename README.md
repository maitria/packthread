# packthread

https://github.com/maitria/packthread

"Heavy" threading macros.

## Why?

Because every time you've wanted to:

```clojure
(-> 42
  (let [x 79]
    (+ x))
  inc)
```

but clojure wouldn't let you.

### `+>`

Threads value through forms in much the same way as `->`, except for special
handling of the following forms:
  
####  if, if-not, if-let, if-some, when, when-let, when-not, when-some

The value is threaded through the *then* and *else* clauses independently,
leaving the test conditions alone.  If an else clause is missing, it is
will be supplied as though the value had been threaded through `identity`.

For example,

```clojure
(+> 42 (if true inc)) ;=> 43
(+> 42 (if false inc)) ;=> 42
```
      
In `when`, `when-let`, `when-not`, and `when-some` forms, the value is
threaded through every form in the body, not just the last.

#### case

The values being compared are left untouched and the value is threaded through
the expr clause of each condition.

For example,

```clojure
(+> 42
  (case 1
    1 inc
    2 dec)) ;=> 43

(+> 42
  (case 2
    1 inc
    2 dec)) ;=> 41
```

#### cond

The test clauses are left untouched and the value is threaded through the expr
clauses of each condition.  If there's no `:else` condition, `+>` pretends it
was `(identity)`.

For example,

```clojure
(+> 42
  (cond
    (= 1 2)
    inc)) ;=> 42

(+> 42
  (cond
    (= 1 1)
    dec)) ;=> 41
```

#### do
    
The current expr is threaded through the body forms of the do.

#### let

The current expression is threaded through the body of the let form, with the
bindings in place. For example:

```clojure
(+> 42 
  (let [x 1] 
    (+ x))) ;=> 43
```

#### try

The current expression is threaded through the body of the `try` form.  The
_same_ value is threaded through each `catch` clause and any `finally` clause.

```clojure
(+> 42 (try
         inc
	 (catch Exception e
	   dec)) ;=> 43

(+> 42 (try
         (+ :foo)
	 (catch Exception e
	   dec))) ;=> 41

(+> 42 (try
         inc
	 (finally dec))) ;=> 42
```

#### in

Threads inner expressions through a [lens] of value.

lens is a function with two arities - 1 and 2.  The 1-arity body is the "get"
function, while the 2-arity body is the "putback" function.  "get" lifts the
value into the new context, while "putback" translates the value back to the
original context.

For example,

```clojure
(+> 42
(in (fn 
      ([v] (/ v 2))
      ([v u] (* u 2)))
  inc)) ;=> 85/2
```

This can be thought of as 'lifting' the body expressions into the 'world
where things are half as large'.

As a special case, if lens is a keyword, in assumes that value is a
map and that sub-key are threaded through the inner expressions.

For example,

```clojure
(+> {:hello 42}
(in :hello
  (+ 5))) ;=> {:hello 47}
```

This macro can only be used inside +> or +>>.

### `+>>`

Threads expressions like `->>`, except with the handling of the special forms
above.

### `fn+>`

Like fn, except threads the function's first argument through the body using
+> .  The parameter vector can be omitted, in which case the resulting function
takes one parameter.

```clojure
((fn+> [x y] (+ x)) 7 3) ;=> 14
((fn+> inc inc) 42) ;=> 44
```

## Installing

[Leiningen](http://github.com/technomancy/leiningen/) dependency information:

```
[com.maitria/packthread "0.1.10"]
```

## Usage

```clojure
(require '[packthread.core :refer :all])

(+> 42
    (if true
      inc)) ;=> 43
```

See [core_test.clj](test/packthread/core_test.clj) for examples of usage.

## License

Copyright 2014 Maitria

You have permission to use this in any way you like (modify it, sell it, republish it), 
provided you agree to all the following conditions:

* you don't mislead anyone about it
* you don't interfere with our ability to use it
* you release us from any claims of liability if it causes problems for you

[lens]: http://repository.upenn.edu/cgi/viewcontent.cgi?article=1044&context=cis_reports
