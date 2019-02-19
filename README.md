Architecture Enforcer
=====================

Architecture analyzer/enforcer for Java codebases.

Compares a codebase's current state to a desired target state, identifying and reporting on all references that violate the target architecture.

This tool is not prescriptive about how you define your architecture, other than identifying illegal references.

## Defining Target State ##

The target state is defined in a yaml file. The file defines layers, domains, and components, all of which are logical groupings that exist "virtually" on top of whatever snarl constitutes the current codebase.

### Layers ###

Layers provide a way to stratify the code, so calls only go in the desired direction.

Layers consist of a unique name and depth, plus a description.

Depth can be any integer (positive, negative, or zero) that has not already been used for another layer. The larger the depth, the higher the layer.

### Domains ###

Domains consist of a unique name, plus a description.

Domains provide a way to group multiple components.

Domains are entirely optional.

If domains are used, every component must belong to exactly one domain.

Layers have no relationship to domains, and vice-versa.

### Components, Packages, And Classes ###

Components provide a way to group packages.

Components consist of a unique name, plus a description, layer, optional domain, and list of 0-N fully-qualified Java package names.

A package belongs to exactly one component.

Packages not listed in the target-state file are rolled up to the nearest enclosing package that is listed. For example, if com.foo.utils is listed in a Utils component, and com.foo.utils.math is not listed anywhere, com.foo.utils.math winds up belonging to the Utils component.

Different subpackages can be assigned to different components. For example, com.foo.utils.math could be assigned to a Math component, com.foo.utils.strings could be assigned to a Strings component, and com.foo.utils could be assigned to a Utils component.

Individual classes cannot be split out from packages and assigned to different components. The finest granularity is the fully-qualified package name. But this can often be easily remedied by moving classes to new subpackages.

All classes must wind up in a component, or the tool fails with an error (the target state must be completely specified).

Layers, domains, and packages referred to by a component must exist, or the tool fails with an error.

Components are somewhat analogous to Java 9 modules. We chose not to make them be modules, because large Java codebases tend to be legacy codebases, which tend to be on earlier versions of Java that don't support modules.

## References ##

Classes typically refer to other classes.

A class in a component can refer to one or more classes in another component, and/or in the same component.

Intra-component references are ignored.

Inter-component references are allowed, so long as the referring component is in a layer higher than the referred-to component.

Inter-component references that refer to components in higher layers, or in the same layer, are illegal. Those are the references this tool detects and reports.

Note that this means circular references among components are illegal.

### Nested Classes And References ###

By default, nested classes are rolled up to the outermost enclosing class, and references to and from nested classes are rolled up to the outermost enclosing classes.

For example, if foo.bar.utils.Utils$Common refers to foo.bar.utils.math.Math$Multiply, the tool registers this as a reference from foo.bar.utils.Utils to foo.bar.utils.math.Math.

In many projects this greatly shrinks the number of references that need to be analyzed.

If it is important in your project to track references at the nested-class level, you need to extract nested classes to new source files, or change the Flags object passed into mainImpl to preserve nesting.

### Kinds Of References ###

Code does not refer to other code just by calling it.

Code can refer to other code by strings, either directly via Class.forName, or indirectly by using things like JSP and Spring, which in turn wind up calling Class.forName.

Code can also be coupled to other code by messages (weak coupling, but that's still a dependency), and in the database (foreign keys), etc.

This tool only finds direct references, and supports manually listing known reflection-based references.

### Removing Illegal References ###

Over time, a team can use various techniques to move the actual state of the codebase towards the desired state, gradually eliminating illegal references. For example, calls from one implementation to another can be replaced with calls to APIs,
shared types can be pushed down from APIs into lower-level components, etc.

Once there are no illegal references, code can be forklifted into separate projects (for example, separate Maven projects, or modules). It seems that this would protect the architecture, because Maven/modules don't allow circular
dependencies among projects. Unfortunately, Maven/modules are compiler-based, and don't understand the various other sneaky ways the architecture can be violated (for example, via reflection). So, even after physically moving
code into projects, this tool should continue to be run in CI/CD.

## Getting Started ##

1. Download the pf-CDA zip from http://www.dependency-analyzer.org, and unpack it into some temp directory. The pf-CDA tool is free to use in binary form (the source is not available). In case the developer changes the licensing or takes down his site, the latest free version is checked into this github.

1. Create a war file for your project. This is necessary even if your project isn't deployed as a war, because pf-CDA requires a war (or at least works best when pointed at a war).

1. Run pf-CDA on your project's war file:

	1. cd to the directory into which you unpacked the zip.
	1. Run ./cda.sh or ./cda.bat, depending on your command line and/or OS.
	1. Select File -> New workset...
	1. On the Classpath tab, click Add, navigate to the war for your project, select it, and click OK.
	1. On the General tab, give the workset a name, check "Reload this workset automatically at next start", and click Save.
	1. When pf-CDA finishes analyzing your project, right-click on the analysis, select Export Model -> XML/ODEM File, give the ODEM file a name, and click Save.
	1. Shut down pf-CDA.

1. Sync and build this project.

1. Using the provided example yaml file as a starting point, define the target state for your project. This can take weeks for a large project, but you can start by just defining a few basic layers (for example, data, logic, and UI), then iterate.

1. Run this tool with at least the first two args specified. You can run this tool from Eclipse or Intellij, or from the command line in the target directory with the command: java -jar architecture-enforcer-1.0-SNAPSHOT.jar.

The full set of args is:

> /full/path/to/target/architecture/.yaml

> /full/path/to/pf-CDA/.odem

> -i/full/path/to/packages/to/ignore

> -r/full/path/to/reflection/references

> -f/full/path/to/fixed/unresolveds

The last three args are optional, and can appear in any order (or not at all). For details, see the notes below.

Notes:

* Typically a project uses a bunch of third-party classes, and/or classes from inside your company but outside the project being decomposed. List packages to ignore in a file you specify with the -i command-line argument.
The syntax is full.name.of.package, without a dot at the end. The tool appends dots for you. In some cases you need to suppress dots due to some issues with pf-CDA, in which case end the package name with a !.

* If your project uses reflection, you should add outermost class-to-class dependencies to a file you specify with the -r command-line argument. The syntax is: full.name.of.referring.class.Foo:full.name.of.referred.to.class.Bar,full.name.of.referred.to.class.Baz....,
where the referred-to classes are classes to which the referring class refers by reflection. At least one referred-to class is required. If there are too many referred-to classes to fit cleanly on one line, you can start multiple lines with the referring class.

* Sometimes the pf-CDA odem file is missing classes that are referred to by other classes in the file. To fix these, you should add the missing outermost classes to a file you specify with the -f command-line argument.
The syntax is: full.name.of.missing.class.Foo:full.name.of.referred.to.class.Bar,full.name.of.referred.to.class.Baz..., where the referred-to classes are classes to which the missing class refers.
If there are too many referred-to classes to fit cleanly on one line, you can start multiple lines with the referring class. If the unresolved class you are adding does not refer to other classes in your project,
you don't need to add any referred-to classes (and you don't need a colon after the first class on the line).

* Adding a referred-to class to the reflections or fix-unresolveds files can introduce new unresolved classes. When that happens, you need to keep entering classes until all classes are defined.

* pf-CDA is smart enough to add references on its own for simple Class.forName calls where the string name of the class is directly specified, as in Class.forName("com.foo.bar.Baz"), but it can't follow complicated string concatenations, strings returned by functions, etc.,
for example Class.forName(someStringFromAVariable + SomeClass.someFunction(some args from somewhere) + SOME_STRING_CONSTANT + ".foo"). That's why you have to add them manually. Also, pf-CDA doesn't parse reflection references in JSP files, Spring, etc.

* Sample files are located in the test resources directory.

## Useful Patterns ##

When defining your project's target state, there are some useful patterns you might want to use.

Components can be "simple", where both the API and implementation of the component are combined, or can be "paired", where the API and implementation are separated.

Paired components move the codebase in the direction of dependency injection, where the implementations chosen at runtime (including during testing) can vary without consumers of the API portions of components being aware anything has changed.

To define target state this way:

* Put the APIs for paired components in a single layer.

* Put the implementations of paired components in a single layer that is one level above the APIs layer.

* Put simple components in one or more layers below the APIs layer.

In this scheme:

* In paired components, implementations can depend on their APIs and on the APIs of other implementations, and can depend on simple components, but APIs can't depend on other APIs; and implementations can't depend on other implementations (other than by dependency injection).

* Simple components can depend on other simple components below them, but never on APIs or implementations for paired components.

Notes:

* Simple components provide no way to prevent other components (in higher levels) from depending on their internals. But for shared low-level utilities that is often fine.

* Teams that wish to avoid the tedium of specifying N low-level simple components can just define a single component that contains all shared types, utilities, etc. However, depending on the codebase, this can create a single component containing a million lines (or more) of code.

* Incremental compilation is a nice side effect of this approach. So long as a programmer only changes the code in the implementation of a paired component, recompilation is limited to just that implementation. Once Mavenized (or moved into modules) this can reduce cycle time from minutes to seconds (not counting time to redeploy).

## TODOs And Welcomed Contributions ##

This tool can of course be improved. Here are some things we know would make it better:

* First, and most obviously, having to manually run pf-CDA at the outset is a pain, plus it thwarts automating analysis in CI/CD. The documentation on http:www.dependency-analyzer.org mentions an API that could probably be called by this tool. Or we could investigate https:innig.net/macker, or javaparser.org.
Alternatively, someone skilled with bytecode analysis could probably replace pf-CDA entirely (we don't need all of its features, just a dump of class-to-class references).

* Add support for lists of classes to components in the target state specification. This would allow splitting packages in cases where creating subpackages is not an option. It also would provide a way to specify where classes in the default package (that is, no package) logically belong.

* Various flags are supported by the implementation, but are not currently offered on the command line. They could be added as more optional arguments, parsed, and used to create the Flags object passed to the implementation.

* Parse Class.forName calls in JSP pages and add those references automatically, instead of requiring manual bookkeeping in the reflection-references file.

* Identify reflection references due to Spring, and add those references automatically.

* Provide a way to fail builds if the count of illegal references increases. (While refactoring, there are often temporary increases in the number of illegal references, so support would also need to be added for whitelisting new illegal references. Access to the whitelist could be restricted to just the team doing decomposition.)

* Add a Maven mojo that calls EnforcerUtils directly (instead of via args in the Enforce main method), and document how to integrate the mojo into builds. (Note that this provides a straightforward solution to automating failing builds when illegal-reference counts increase.)

* Provide front-end code that displays a burndown chart based on the count of illegal references, and provide a way to integrate this into CI/CD pipelines.

* Support regular expressions where currently individual packages or classes have to be specified. Important note: This will break how we roll up to the nearest enclosing package, plus more than one pattern might resolve to the same classes, which would need to be reported as an error; so this might be a bad idea.

We welcome contributions of those and other improvements.

## Implementation Notes ##

This tool is a cleanroom reimplementation of a proprietary tool used for a massive decomposition project.

Only general, well-known refactoring concepts have been reused (layering, encapsulation, APIs, implementation, etc.). Nothing from the proprietary tool's code was used, and this tool differs significantly from how that tool worked.

## Caveats ##

It's possible this tool can miss some dependencies.

For example, pf-CDA determines dependencies from bytecode, and static constants are inlined in bytecode without any "backpointer" to the defining class (it might be possible to fix this by looking at debug symbols).

In addition, the reflection-based references and unresolved fixes, being entered manually, are only as good as your team's ability to find them all.

But overall, this tool can probably get it > 95% correct, which is close enough to start trying to actually move the decomposed code, at which point some gotchas will likely pop up that need to be dealt with.

## Copyright ##

All files in this github except for the pf-CDA zip are subject to the following MIT license:

  Copyright 2019 jimandlisa.com.

  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  
  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
