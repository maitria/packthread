# packthread

Threading macros for working with globs of state.

## Why?

Many descriptions about state in Clojure fit into the following form:

> State is hard to reason about, and so we use pure functions in Clojure.  But then
> we have a problem, we need to pass around _all_ the application's state. And that's
> just too hard, and it's basically just like having all the global variables anyway,
> and you've coupled every function in the system to this big ball of mud. So we need
> to separate it out and encapsulate and therefore we've invented $library which does
> _x_, where _x_ ∈ {_OO programming_, _global mutable state_, _..._}.

Packthread is for threading state through programs in a simple, composable way.  It
does not compromise the ability to be functionally pure or reason about one's program.
It's pretty similar to the `->` and `->>` macros, with a helper macro named `in` for
creating different _projections_ of the state to manipulate with different functions.

### `+>`

Threads value through forms in much the same way as `->`, except for special
handling of the following forms:
  
####  if, if-not, if-let, when, when-not, when-let:

The value is threaded through the then and else clauses independently,
leaving the test conditions alone.  If an else clause is missing, it is
will be supplied as though the value had been threaded through identity
in that case.

For example,

```clojure
(+> 42 (if true inc)) ;=> 43
(+> 42 (if false inc)) ;=> 42
```
      
In `when`, `when-not`, and `when-let` forms, the value is threaded through each
form in the body, not just the last.

#### cond

The test clauses are left untouched and the value is threaded through
the expr clauses of each condition.  If no :else condition was supplied,
`+>` pretends as though it has been (identity), and threads the value
through that.

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

#### in

Threads the inner expressions through a projection of value.

projector is a function which takes two arguments: a value and a function.
It should apply the function to a _projection_ of the value, take the
function's result, and reassemble from that result a value which can be
used again in the outer context.

For example,

```clojure
(+> 42
    (in (fn [v f]
	  (* 2 (f (/ v 2))))
      inc)) ;=> 42.5
```

This can be thought of as 'lifting' the body expressions into the 'world
where things are twice as large'.

As a special case, if projector is a keyword, in assumes that value is a
map and that sub-key are threaded through the inner expressions.

For example,

```clojure
(+> {:hello 42}
    (in :hello
      (+ 5))) ;=> {:hello 47}
```

This macro can only be used inside `+>` or `+>>`.

### `+>>`

Threads expressions like `->>`, except with the handling of the special forms
above.

## Installing

[Leiningen](http://github.com/technomancy/leiningen/) dependency information:

```
[com.maitria/packthread "0.1.0"]
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
