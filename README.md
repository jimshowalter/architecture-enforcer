Architecture Enforcer
=====================

Architecture analyzer/enforcer for Java codebases.

Compares a codebase's current state to a desired target state, identifying and reporting on all references that violate the target architecture.

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

### Components, Packages, And Classes ###

Components provide a way to group packages.

Components consist of a unique name, plus a description, layer, optional domain, and list of 0-N fully-qualified Java package names.

A package belongs to exactly one component.

Packages not listed in the target-state file are rolled up to the nearest enclosing package that is listed. For example, if com.foo.utils is listed in a Utils component, and com.foo.utils.math is not listed anywhere, com.foo.utils.math winds up belonging to the Utils component.

Different subpackages can be assigned to different components. For example, com.foo.utils.math could be assigned to a Math component, com.foo.utils.strings could be assigned to a Strings component, and com.foo.utils could be assigned to a Utils component.

Individual classes cannot be split out from packages and assigned to different components. The finest granularity is the fully-qualified package name. But this can be easily remedied by simply moving classes to new subpackages.

Nested classes are rolled up to the outermost enclosing class.

All classes must wind up in a component, or the tool fails with an error (the target state must be completely specified).

Layers, domains, and packages referred to by a component must exist, or the tool fails with an error.

#### Kinds Of Components ####

Components can be simple, where both the API and implementation of the component are combined, or can be paired, where the API and implementation are separated.

Paired components move the codebase in the direction of dependency injection, where the implementation chosen at runtime (including during testing) can vary without consumers of the API portion of the component being aware anything has changed.

Components are somewhat analogous to Java 9 modules. We chose not to make them be modules, because large Java codebases tend to be legacy codebases, which tend to be on earlier versions of Java that don't support modules.

## References ##

### Rules ###

The rules for references are:

1. All APIs for paired components are in a single layer.
1. All implementations of paired components are in a single layer.
1. The implementations layer is above the APIs layer.
1. All simple components are in one or more layers below the APIs layer.
1. Code in a component can only refer to code in the same component, or to code in components in lower layers.

References that violate the above rules are identified and reported by this tool.

To summarize: In paired components, implementations can depend on their APIs and on the APIs of other implementations, and can depend on simple components, but APIs can't depend on other APIs, and implementations can't depend on other implementations (other than by dependency injection);
and simple components can depend on other simple components below them, but never on APIs or implementations for paired components.

Notes:

* This approach winds up putting common types and utilities in N layers of low-level shared simple components, and puts API/impl pairs on top of the low-level stuff in a clean layering.

* Teams that wish to avoid the tedium of specifying N low-level simple components can just define a single component that contains all shared types, utilities, etc. However, depending on the codebase, this can create a single component containing a million lines (or more) of code.

* Incremental compilation is a nice side effect of this approach. So long as a programmer only changes the code in the implementation of a paired component,  recompilation is limited to just that implementation. Once Mavenized (or moved into modules) this can reduce cycle time from minutes to seconds (not counting time to redeploy).

* References to and from nested classes are rolled up to the outermost enclosing classes. For example, if foo.bar.utils.Utils refers to foo.bar.utils.math.Math$Multiply, the tool registers this as a reference from foo.bar.utils.Utils to foo.bar.utils.math.Math.
In many projects this greatly shrinks the number of references that need to be analyzed. If it is important in your project to track references at the nested-class level, you need to extract nested classes to new source files, or modify the tool so it preserves nesting
(search for "nesting" in EnforcerUtils).

### Kinds Of References ###

Code does not refer to other code just by calling it.

Code can refer to other code by strings, either directly via Class.forName, or indirectly by using things like JSP and Spring, which in turn wind up calling Class.forName.

Code can also be coupled to other code by messages (weak coupling, but that's still a dependency), and in the database (foreign keys), etc.

This tool only finds direct references, and supports manually listing known reflection-based references.

### Removing Illegal References ###

Over time, a team can use various techniques to move the actual state of the codebase towards the desired state, gradually eliminating illegal references. For example, calls from one implementation to another can be replaced with calls to APIs,
shared types can be pushed down from APIs into lower-level simple components, etc.

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
	1. When pf-CDA finishes analyzing your project, select File -> Export Model -> XML/ODEM File, give the ODEM file a name, and click Save.
	1. Shut down pf-CDA.

1. Sync and build this project.

1. Using the provided example yaml file as a starting point, define the target state for your project. This can take weeks for a large project, but you can start by just defining a few basic layers (for example, data, logic, and UI), then iterate.

1. Run this tool with at least the first two args specified. You can run this tool from Eclipse or Intellij, or from the command line in the target directory with the command: java -jar architecture-enforcer-1.0-SNAPSHOT.jar.

The full set of args is:

> /full/path/to/target/architecture/.yaml

> /full/path/to/pf-CDA/.odem

> -i/full/path/to/packages/to/ignore

> -r/full/path/to/reflection/references

The last two args are optional, but see the notes below.

Notes:

* Typically a project uses a bunch of third-party classes, and/or classes from inside your company but outside the project being decomposed. List packages to ignore in a file you specify with the -i command-line argument.
The syntax is full.name.of.package, without a dot at the end. The tool appends dots for you. In some cases you need to suppress dots due to some issues with pf-CDA, in which case end the package name with a !.

* If your project uses reflection, you should add outermost class-to-class dependencies to a file you specify with the -r command-line argument. The syntax is: full.name.of.referring.class.Foo:full.name.of.referred.to.class.Bar.

## TODOs And Welcomed Contributions ##

This tool can of course be improved. Here are some things we know would make it better:

* First, and most obviously, having to manually run pf-CDA at the outset is a pain, plus it thwarts automating analysis in CI/CD. The documentation on http:www.dependency-analyzer.org mentions an API that could probably be called by this tool. Or we could investigate https:innig.net/macker, or javaparser.org.
Alternatively, someone skilled with bytecode analysis could probably replace pf-CDA entirely (we don't need all of its features, just a dump of class-to-class references).

* Add an option to preserve nesting. Default the option to disabled (don't preserve nesting) to avoid memory/time overhead.

* Parse Class.forName calls in JSP pages and add those references automatically, instead of requiring manual bookkeeping in the reflection-references file.

* Identify reflection references due to Spring, and add those references automatically. (This is a big job!)

* Provide a way to fail builds if the count of illegal references increases. (While refactoring, there are often temporary increases in the number of illegal references, so support would also need to be added for whitelisting new illegal references. Access to the whitelist could be restricted to just the team doing decomposition.)

* Provide front-end code that displays a burndown chart based on the count of illegal references, and provide a way to integrate this into CI/CD pipelines.

* Add a Maven mojo that calls EnforcerUtils directly (instead of via args in the Enforce main method), and document how to integrate the mojo into builds. (Note that this provides a straightforward solution to automating failing builds when illegal-reference counts increase.)

We welcome contributions of those and other improvements.

## Implementation Notes ##

This tool is a cleanroom reimplementation of a proprietary tool used for a massive decomposition project.

Only general, well-known refactoring concepts have been reused (layering, encapsulation, etc.). Nothing from the proprietary tool's code was used, and this tool differs significantly from how that tool worked.


## Caveats ##

It's possible this tool can miss some dependencies.

For example, pf-CDA determines dependencies from bytecode, and static constants are inlined in bytecode without any "backpointer" to the defining class.

In addition, the reflection-based references, being entered manually, are only as good as your team's ability to find them all.

But overall, this tool can probably get it > 95% correct, which is close enough to start trying to actually move the decomposed code, at which point some gotchas will pop up that need to be dealt with.

## Copyright ##

All files in this github except for the pf-CDA zip are subject to the following MIT license:

// Copyright 2019 jimandlisa.com.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.