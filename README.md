Architecture Enforcer
=====================

Architecture analyzer/enforcer for Java codebases.

Compares a codebase's current state to a desired target state, identifying and reporting on all references that violate the target architecture.

A companion project, https://github.com/jimshowalter/architecture-enforcer-sample, provides a sample war used by this project's tests, and in this documentation.

## Defining Target State ##

A target state is defined in terms of layers, domains, and components, all of which are logical groupings that exist "virtually" on top of whatever snarl constitutes the current codebase.

Overlaying a virtualized target state allows you to perform iterative what-if analysis before changing any of the actual code.

How you define your target state is up to you&mdash;this tool is not prescriptive, other than prohibiting illegal references.

The target state is defined in a yaml file that is the single source of truth for this tool.

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

Individual classes can be split out from packages and assigned to different components by specifying classes belonging to the components. However, this should be treated as temporary, because when code is relocated to Maven projects (or Java modules),
best practice is for every package to belong to exactly one project/module. So, where possible, moving classes to different packages is preferable.

Classes belonging to the default package (that is, not having a package) can be assigned to components by specifying them in the components.

All classes must wind up in a component, or this tool fails with an error (the target state must be completely specified).

Layers, domains, packages, and classes referred to by a component must exist, or this tool fails with an error.

Components are somewhat analogous to Java 9 modules. We chose not to make them be modules, because large Java codebases tend to be legacy codebases, which tend to be on earlier versions of Java that don't support modules.

## References ##

Classes typically refer to other classes.

A class in a component can refer to classes in the same component and in different components.

Only some references between components are legal.

This tool classifies references into one of three buckets:

* INTRA: Intra-component. Always legal.

* LEGAL: Inter-component. Legal because dependency is from higher layer to lower layer.

* ILLEGAL: Inter-component. Illegal because dependency is "sideways" (between components in same layer), or from lower layer to higher layer. (Note that this means circular references among components are illegal, even in the same layer.)

To summarize:

|Referring Component Layer|Referred-To Component Layer|Legal?|
|:------------------------|:--------------------------|:-----|
|N+1|N|Yes|
|N|N+1|No|
|N|N (same component as referring component)|Yes|
|N|N (different component than referring component)|No|

In a sense, the entire point of this tool is to be able to implement these three functions:

```
	public boolean isIntraComponentReference() {
		return referringType.component().equals(referredToType.component());
	}
	
	public boolean isInSameOrLowerLayer() {
		return referringType.component().layer().depth() <= referredToType.component().layer().depth();
	}

	public boolean isLayerViolation() {
		if (isIntraComponentReference()) {
			return false;
		}
		return isInSameOrLowerLayer();
	}
```

### Nested Classes And References ###

By default, nested classes are rolled up to the outermost enclosing class, and references to and from nested classes are rolled up to the outermost enclosing classes.

For example, if foo.bar.utils.Utils$Common refers to foo.bar.utils.math.Math$Multiply, this tool registers this as a reference from foo.bar.utils.Utils to foo.bar.utils.math.Math.

In many projects this greatly shrinks the number of references that need to be analyzed.

If it is important in your project to track references at the nested-class level, you need to extract nested classes to new source files, or specify the -p option to preserve nested types.

### Sneaky References ###

Code does not refer to other code just by importing or calling it.

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

1. Create a war file for your project. This is necessary even if your project isn't deployed as a war, because pf-CDA, which we use to analyze bytecode, requires a war (or at least seems to work best when pointed at a war).

1. Sync and build this project. Ignore "[WARNING] The POM for org.apache.bcel:bcel:jar:6.3.PR1 is missing, no dependency information available" and "[WARNING] Classes in bundle 'Architecture Enforcer' do no [sic] match with execution data. For report generation the same class files must be used as at runtime".
We're working on fixing those warnings (and welcome your help, if you know how to make them go away).

1. Verify that the project works, by running this command in the target directory (adjusted for your environment):

```
java -jar architecture-enforcer-1.0-SNAPSHOT.jar /path/to/architecture-enforcer/target/test-classes/SampleTarget2.yaml /path/to/architecture-enforcer/target/test-classes/architecture-enforcer-sample-1.0-SNAPSHOT.war /path/to/architecture-enforcer/target -i/path/to/architecture-enforcer/target/test-classes/SampleIgnores.txt
```

4. (Optional) Run this tool from Eclipse or IntelliJ.

5. Using one of the provided sample yaml files as a starting point, define the target state for your project. This can take weeks for a large project, but you can start by just defining a few basic layers (for example, data, logic, and UI), then iterate.
(It's probably better to start small anyway, instead of trying to boil the ocean in one shot.)

We provide two sample yaml files. SampleTarget1.yaml is the ball-of-mud target state, where everything is in one layer and component (and no domains are used). SampleTarget2.yaml demonstrates a layered target state, with domains. You can start with either sample, plus SampleIgnores.txt, and go from there.

To run this tool with SampleTarget1.yaml, just change 2 to 1 in the above command.

For large codebases, this tool requires lots of memory. Make sure you provide enough.

For large codebases, this tool can take 30+ seconds to run, even when given lots of memory.

## Command-line Arguments ###

The full set of args is:

> /full/path/to/target/architecture/.yaml

> /full/path/to/.war

> /full/path/to/writable/output/directory

> -UunresolvedTypesOutputFileSimpleName

> -IillegalReferencesOutputFileSimpleName

> -A

> -i/full/path/to/file/of/packages/and/classes/to/ignore

> -r/full/path/to/file/of/reflection/references

> -f/full/path/to/file/of/fixed/unresolveds

> -p (preserves nested types)

> -s (strict, requires that all types resolve and no illegal references)

> -d (debug)

Run this tool with at least the first three args specified.

The first two args specify input files. The third arg specifies the directory where output files go.

The remaining nine args are optional, and can appear in any order (or not at all).

Unresolved types and illegal references are written to the output directory (if strict is not specified).

Unresolved types are written as the fully-qualified type name, one type per line.

By default, the unresolved-types output file name is "unresolved\_types.txt", and the illegal-references output file name is "illegal\_references.txt". These can be overridden with the -U and -I options, respectively.

If -A is specified, all references (not just illegal references, and including intra-component references) are output to additional files.
The basic files are all\_references.txt, which contains all class-to-class references, and all\_component\_references.txt, which contains all component-to-component references.
Other files are generated for https://gephi.org (all\_references\_GephiNodes.csv, all\_references\_GephiEdges.csv, all\_component\_references\_GephiNodes.csv, all\_component\_references\_GephiEdges.csv) and https://www.yworks.com/products/yed (all\_references\_yed.tgf and all\_component\_references\_yed.tgf).
Because there are numerous files and specific suffixes are required, the names of -A files cannot be specified from the command line.

Notes:

* Typically a project uses a bunch of third-party classes, and/or classes from inside your company but outside the project being decomposed. List packages and classes to ignore in a file you specify with the -i command-line argument.
The syntax is full.name.of.package(.AndOptionallyTheClass). By default, this tool appends dots for you. In some cases you need to suppress dots due to some issues with pf-CDA, in which case end the package or class name with a !.
In other cases, you need to ignore a class in the default package (that is, no package), in which case just list the class name, ending with a !.

* If your project uses reflection, you should add class-to-class dependencies to a file you specify with the -r command-line argument. The syntax is: full.name.of.referring.class.Foo:full.name.of.referred.to.class.Bar,full.name.of.referred.to.class.Baz....,
where the referred-to classes are classes to which the referring class refers by reflection. At least one referred-to class is required. If there are too many referred-to classes to fit cleanly on one line, you can start multiple lines with the referring class.
If you preserve nested types in your analysis, and you have instances of reflection references from or to nested types, include $TheNestedType in the names.

* Sometimes pf-CDA misses classes that are referred to by other classes in the war. To fix these, you should add the missing classes to a file you specify with the -f command-line argument.
The syntax is: full.name.of.missing.class.Foo:full.name.of.referred.to.class.Bar,full.name.of.referred.to.class.Baz..., where the referred-to classes are classes to which the missing class refers.
If there are too many referred-to classes to fit cleanly on one line, you can start multiple lines with the referring class. If the unresolved class you are adding does not refer to other classes in your project,
you don't need to add any referred-to classes (and you don't need a colon after the referring class on the line). If you preserve nested types in your analysis, and you have instances of unresolved types from or to nested types,
include $TheNestedType in the names.

* Adding a referred-to class to the reflections or fix-unresolveds files can introduce new unresolved classes. When that happens, you need to keep entering classes until all classes are defined.

* This tool creates unresolved types and adds them to the type-lookup map so downstream analysis doesn't blow up. This is just a band-aid through, because whatever types a missing type refers to are not included in the analysis (because they aren't known).

* pf-CDA is smart enough to add references on its own for simple Class.forName calls where the string name of the class is directly specified, as in Class.forName("com.foo.bar.Baz"), but it can't follow complicated string concatenations, strings returned by functions, etc.,
for example Class.forName(someStringFromAVariable + SomeClass.someFunction(some args from somewhere) + SOME\_STRING\_CONSTANT + ".Foo"). That's why you have to add them manually. Also, pf-CDA doesn't parse reflection references in JSP files, Spring, etc.

* If the target state only contains one component, there can't be any illegal references.

* Sample files are located in the src/test/resources directory. They start with "Sample".

## References Formats ##

References are written in a format designed to be easy to machine read:

```
referringType!referringComponent!referringLayer!referringDepth!referredToType!referredToComponent!referredToLayer!referredToDepth!(INTRA|LEGAL|ILLEGAL)
```

For example:

```
com.jimandlisa.app.one.App1!App One!App!1!com.jimandlisa.app.two.App2!App Two!App!1|ILLEGAL
```

The output format can be sliced and diced by any number of analysis tools. For example, it can be sorted into a histogram of most-illegally-referred-to components, or most-offending classes, etc. This can help the team decomposing the project figure out what to focus on first.

Component references are similar, but without class or layer information:

```
App One!App One!INTRA
App One!App Two!ILLEGAL
```

Focusing on illegal component-to-component references can help teams understand the challenges in the codebase without getting mired in tens of thousands of illegal class-to-class dependencies.

The Problem objects for illegal references have a detail field that presents the same information in a more human-readable format:

```
type com.jimandlisa.app.one.App1 in component 'App One' in layer 'App' depth 1 refers to type com.jimandlisa.app.two.App2 in component 'App Two' in layer 'App' depth 1
```

## Useful Patterns ##

When defining your target state, there are some useful patterns you might want to use.

Components can be "simple", where both the API and implementation of the component are combined, or can be "paired", where the API and implementation are separated.

Paired components:

* Move the codebase in the direction of dependency injection, where the implementations chosen at runtime (including during testing) can vary without consumers of component APIs being aware anything has changed.

* Enforce the idea that calls are only allowed to "come in through the front door".

To define your target state this way:

* Put the APIs for paired components in a single layer.

* Put the implementations of paired components in a single layer that is one level above the APIs layer.

* Put simple components in one or more layers below the APIs layer.

In this scheme:

* In paired components, implementations can depend on their APIs and on the APIs of other implementations, and can depend on simple components, but APIs can't depend on other APIs, and implementations can't depend on other implementations.

* Simple components can depend on other simple components below them, but never on APIs or implementations for paired components.

Notes:

* Simple components provide no way to prevent other components (in higher levels) from depending on their internals. But for shared low-level utilities that is often fine.

* Teams that wish to avoid the tedium of specifying N low-level simple components in M little layers can just define a single "platform" component that contains all shared types, utilities, etc. in one layer. However, depending on the codebase, this can create a single component containing a million lines (or more) of code.

* Incremental compilation is a nice side effect of this approach. So long as a programmer only changes the code in the implementation of a paired component, recompilation is limited to just that implementation.
Once Mavenized (or moved into modules) this can reduce cycle time from minutes to seconds (not counting time to redeploy).

* You may need to define some "shim" layers between APIs and impls, for example to share some common types that you don't want to push down below the APIs. That's fine&mdash;do whatever makes sense for your target state.

## Problem Kinds ##

Problems are either warnings, or errors.

Warnings are never fatal.

Errors are either always fatal, or fatal only if strict is specified.

All but two errors are always fatal:

* Unresolved references are permitted when not in strict mode because sometimes pf-CDA misses classes that are referred to by other classes in the war. This allows a grace period while the missing types are added to the fix-unresolveds file.

* Illegal references are permitted when not in strict mode, so the team working on decomposition can see the report of illegal references without blocking other development.

Once all unresolved and illegal references are fixed, strict mode should be enabled.

## TODOs And Welcomed Contributions ##

This tool can of course be improved. Below are listed some things we know would make it better, plus some things that might or might not be good ideas. We welcome contributions of these and other improvements.

### TODOs We Like ###

* BUG: Classes can start with dollar signs, so the current approach to denesting is erroneous. See if pf-CDA provides a way to determine if a class is nested, and, if so, to get its outermost class.

* PERFORMANCE: Instead of creating the entire graph with pf-CDA (which can be gigantic for large codebases) and then ignoring a bunch of classes, see if there's a way to pass in a filter when initializing the pf-CDA workspace.

* WARNING: Fix the build warnings.

* HYGIENE: Add one or more code-quality tools to the build. For example, findbugs, errorprone, etc.

* HYGIENE: There is repetitive "Utils.called" call-tracking code in the architecture-enforcer-sample project that probably could be simplified via aspects.

* HYGIENE: Start tracking these todos in a bug-tracking system.

* FEATURE: Improve graphics support. For example, add arrows to edges to show depends-on direction, add "illegal" labels to illegal edges and/or color illegal edges red, and add counts of illegal references between components.

* FEATURE: Provide a way to fail builds if the count of illegal references increases. Note that this is different from enabling strict mode, because in that case builds fail if there are any illegal references, so the previous count is known (it's zero).
In contrast, this feature requires determining that there were N illegal references in the previous build, and now there are N + M illegal references in the current build. One way to do this is to access the previous build in CI/CD using something like the Jenkins API.
Note that, while refactoring, there are often temporary increases in the number of illegal references, so decomposition teams would need to be able to temporarily whitelist new illegal references (access to the whitelist could be restricted to just that team).

* FEATURE: Add a Maven mojo that calls EnforcerUtils directly (instead of via args in the Enforce main method), and document how to integrate the mojo into builds. Possibly also provide gradle support.

* FEATURE: Provide front-end code that displays a burndown chart based on the count of illegal references, and provide a way to integrate this into CI/CD pipelines.

* FEATURE: Parse Class.forName calls in JSP pages and add those references automatically, instead of requiring manual bookkeeping in the reflection-references file.

* FEATURE: Identify reflection references due to Spring, and add those references automatically, instead of requiring manual bookkeeping in the reflection-references file. (Check if an open-source project exists that can do this analysis.)

### TODOs We're Unsure About ###

These range from ideas that might be good, but we're not sure have a use, so we're using YAGNI to defer implementation, to ideas that might be awful.

* FEATURE: For teams that want things to be more prescriptive, add component keywords "simple", "api", and "impl", and provide a way to pair related apis and impls (and to enforce rules about no access to impls).

* FEATURE: For teams that need to support multiple implementation layers, add a "private" component keyword, so implementations in layer N + 1 can't call implementations in layer N, even though N + 1 is higher.

* FEATURE: For teams that want to use domains more for tagging/labeling than for grouping, allow components to belong to multiple domains.

* FEATURE: Support regular expressions where currently individual packages or classes have to be specified. Important note: This will break how we roll up to the nearest enclosing package, plus more than one pattern might resolve to the same classes, which would need to be reported as an error; so this might be a bad idea.

## Caveats ##

It's possible this tool can miss some dependencies.

For example, pf-CDA determines dependencies from bytecode, and static constants are inlined in bytecode without any "backpointer" to the defining class (it might be possible to fix this by looking at debug symbols).

In addition, the reflection-based references and unresolved fixes, being entered manually, are only as good as your team's ability to find them all.

But overall, this tool can probably get it > 95% correct, which is close enough to start trying to actually move the decomposed code, at which point some gotchas will likely pop up that need to be dealt with.

## Implementation Notes ##

This tool is a cleanroom reimplementation of a proprietary tool used for a massive decomposition project.

Only general, well-known refactoring concepts have been reused (layering, encapsulation, APIs, implementation, etc.). Nothing from the proprietary tool's code was used, and this tool differs significantly from how that tool worked.

The following table summarizes differences between the two tools:

|Proprietary Tool|This Tool|
|:---------------|:--------|
|Proprietary|Open-source|
|Parses source files and POMs|Uses compiled bytecode|
|Files/directories|Classes/packages|
|Prescriptive (kinds of components, etc.)|Unrestricted|
|Can't split packages across components|Can specify individual classes per component (in addition to packages, or instead of packages)|
|Can't handle classes in default package (that is, no package), requires special-casing in code|Can specify individual classes per component, even without any package|
|Thousands of lines|Fewer than 1k lines|
|Complex|Simple|
|Incompletely unit tested|100% statement and branch coverage|
|Requires Maven to run it|Can run as jar with main, or as Maven mojo|
|Unresolved references special-cased in code|Unresolved references entered in a text file|
|Code full of proprietary special-casing for particular company|No company-specific special-casing|
|Parses string-based references (reflection) in Java (Class.forName), in JSP files, and in other kinds of files, plus supports manual entry of hard-to-parse cases|Requires manual entry of all reflection references|
|Includes line numbers and text of code on line in output of illegal references|Only outputs classes and components|

Why did we choose pf-CDA? It's fast, and it has a simple API (at least for what we needed to do).
There are alternatives, for example http://javaparser.org, https://innig.net/macker, https://commons.apache.org/proper/commons-bcel, https://docs.oracle.com/javase/9/tools/jdeps.htm, etc. There are a few issues with pf-CDA, but it's not clear if any of the alternatives would be an improvement.
For example, regarding misssing types, https://maven.apache.org/shared/maven-dependency-analyzer/index.html says: "Analysis is not done at source but bytecode level, then some cases are not detected (constants, annotations with source-only retention,
links in javadoc) which can lead to wrong result if they are the only use of a dependency."

## See Also ##

https://github.com/jimshowalter/architecture-enforcer-sample

http://www.dependency-analyzer.org (pf-CDA)

## Credits ##

We are grateful to the author of pf-CDA!

And we're very grateful to Steve Hartman for his clear vision of how a target architecture should be articulated.

## Shameless Plug ##

The author of this tool is available to consult on your decomposition project, and charges reasonable rates.

## Copyright/Licensing ##

The pf-CDA tool is free to use in binary form (the source is not available). In case the developer changes the licensing or takes down his site, the latest free version is checked into this github, and we can revert to checking in the unpacked jars needed to build and run pf-CDA (which is how we used to do it, up until commit 0bc63b9ddcf365e846708a265cafec8cb1c138e8).

All files in this github except for the pf-CDA zip and jars are subject to the following MIT license:

  Copyright 2019 jimandlisa.com.

  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  
  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
