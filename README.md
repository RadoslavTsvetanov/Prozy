# SimpleReverseProxy

# Why
- I needed a https reverse proxy with more complex filtering functionality and nginx and traefik weren't good enough
- to learn a new language I wanted to learn for a long time
- for funnies
- I needed to write tests for my redirecting logic and writing them in a real lang would have made it easier

# Features 

## Middlewares 
there is a folder called middlewaresin which every file is executed as middleware, for now only the tool lang is supported buy in the future there will be support for different langs which will be run inside a sandbox env

## Handlers
in the handlers folder again you make a handwr for redirecting logic
which should follow this function signature
```
fn handler(req: Req) -> string | null
```
where the return is the redirect url and if it is null we invoke the nest handleramd like that until one returns a string

also handles are executed in order e.g. First fails we ge the second etc....
## easy to set up

## easy to set up ssl certs

## Built in  and customizable logging
- in the config object (either json file loaded from the config.json or as env) you can specify a file to which to save the logs for each request 
- you can define your custom logging handler which must be called logging.scala and located in the root dir, if you dont want to make one you can sepcify the entry `logging` in your `config.json` to either `all` or `none` and to use a custom one it should be set to `custom`. Also logging handlers are executed in seperate process so dont be afraid that it will hurt latency
NOTE: when loading config it is first checked for he existence of an env and then for a file

## Exposed Api
Dont like json and managing configs, well just like any web server lib you can just pull the dependency in your scala project and start building on top of it. To see more
about this look at `Extending the basic web server`


# Extnding the base web server

## Getting started

### Downloading
- add this to your `build.sbt` 
  
  ```scala
  libraryDependencies += "to do" % "to do " % "1.0.0"
  ```
### Extending the web server
Example
```
import com.to_do.ReverseProxy

class MyCustomReverseProxyImplementation extends ReverseProxy{
}
```

# Examples

## Config
```json
{

}
```
