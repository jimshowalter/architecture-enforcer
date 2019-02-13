Architecture Enforcer
=====================

Architecture analyzer/enforcer for Java codebases.

Compares a codebase's current state to a desired target state, identifying and reporting on all references that violate the target architecture.

## Defining Target State ##

The target state is defined in a file. The file defines layers, domains, and components, all of which are logical groupings that exist "virtually" on top of whatever snarl constitutes the current codebase.

### Layers ###

Layers provide a way to stratify the code, so calls only go in the desired direction.

Layers consist of a unique name and depth, plus a description.

Depth can be any integer (positive, negative, or zero) that has not already been used for another layer.

### Domains ###

Domains consist of a unique name, plus a description.

Domains provide a way to group multiple components.

Domains are entirely optional.

If domains are used, every component must belong to exactly one domain.

### Components, Packages, And Files ###

Components provide a way to group packages.

Components consist of a unique name, plus a description, layer, optional domain, and list of 0-N fully-qualified Java package names.

A package belongs to exactly one component.

Packages not listed in the target-state file are rolled up to the nearest enclosing package that is listed. For example, if com.foo.utils is listed in a Utils component, and com.foo.utils.math is not listed anywhere, com.foo.utils.math winds up belonging to the Utils component.

Different subpackages can be assigned to different components. For example, com.foo.utils.math could be assigned to a Math component, com.foo.utils.strings could be assigned to a Strings component, and com.foo.utils could be assigned to a Utils component.

Individual source files cannot be split out from packages and assigned to different components. The finest granularity is the individual package name. But this can be easily remedied by simply moving files to new subpackages.

All source files must wind up in a component, or the tool fails with an error (the target state must be completely specified).

Layers, domains, and packages that are listed in a component must exist, or the tool fails with an error.

#### Kinds Of Components ####

Components can be simple, where both the API and implementation of the component are combined, or can be paired, where the API and implementation are separated.

Paired components move the codebase in the direction of dependency injection, where the implementation chosen at runtime (including during testing) can vary without consumers of the API portion of the component being aware anything has changed.

Components are somewhat analogous to Java 9 modules. We chose not to make them be modules, because large Java codebases tend to be legacy codebases, which tend to be on earlier versions of Java that don't support modules.

## References ##

### Rules ###

The rules for references are:

1. All APIs for paired components are in a single layer.
2. All implementations of paired components are in a single layer.
3. The implementations layer is above the APIs layer.
4. All simple components are in one or more layers below the APIs layer.
5. Code in a component can only refer to code in the same component, or to code in components in lower layers.

To summarize: implementations can depend on their APIs and on the APIs of other implementations, and can depend on simple components, but APIs can't depend on other APIs, and implementations can't depend on other implementations (other than by dependency injection); and simple components can depend on other simple components below them, but never on APIs or implementations for paired components.

Notes:

* This approach winds up putting common types and utilities in N layers of low-level shared simple components, and puts API/impl pairs on top of the low-level stuff in a clean layering.

* Teams that wish to avoid the tedium of specifying N low-level simple components can just define a single component that contains all shared types, utilities, etc. However, depending on the codebase, this can create a single component containing a million lines (or more) of code.

* Incremental compilation is a nice side effect of this approach. So long as a programmer only changes the code in the implementation of a paired component,  recompilation is limited to just that implementation. This can reduce cycle type from minutes to seconds (not counting time to redeploy).

### Kinds Of References ###

Code does not refer to other code just by calling it.

Code can refer to other code by strings, either directly via Class.forName, or indirectly by using things like JSP and Spring, which in turn wind up calling Class.forName.

Code can also be coupled to other code by messages (weak coupling, but that's still a dependency), and in the database (foreign keys), etc.

This tool (currently) analyzes direct references, and supports listing known reflection-based references.

Contributions of support for parsing Class.forName calls and JSP pages, "looking through" Spring references, etc. are welcomed.

### Illegal References ###

References that violate the rules are identified and reported by this tool.

Over time, a team can use various techniques to move the actual state of the codebase towards the desired state, gradually eliminating illegal references.

The counts of illegal references can be used to generate an up-to-the-moment accurate burndown chart showing how much work remains to fix the codebase. This can be incorporated into a CI/CD pipeline, and reviewed by management.

If desired, builds can be made to fail if the number of illegal references increases, although while refactoring there are often temporary spikes that have to be allowed. To support this, new illegal references can be temporarily whitelisted.

Once the count of illegal references drops to zero, any attempt to add new ones should always fail the build, which protects the architecture.

Once there are no illegal references, code can be forklifted into separate projects (for example, separate Maven projects, or modules). It seems that this would protect the architecture, because Maven/modules don't allow circular
dependencies among projects. Unfortunately, Maven/modules are compiler-based, and don't understand the various other sneaky ways the architecture can be violated (for example, via reflection). So, even after physically moving
code into projects, this tool should continue to be run in CI/CD.

Contributions of burndown-chart displaying code and CI/CD pipeline configurations (including forced build failures) are welcome.

## Implementation Notes ##

This tool is a cleanroom reimplementation of a proprietary tool used for a massive decomposition project.

Only concepts have been reused. The proprietary tool used a completely different approach for identifying dependencies, based on parsing source files, while this tool uses an off-the-shelf parser. Also, the proprietary tool had a number of things specific to that particular codebase that aren't in this reimplementation. And the proprietary tool was a custom Maven mojo, whereas this is just a simple jar with a main (because not all Java projects use Maven).

Regarding the off-the-shelf parser, this tool depends on the output from pf-CDA (http:www.dependency-analyzer.org) as a starting point. pf-CDA is free to use in binary form, but the source code is not available.
It's possible we could use https:innig.net/macker instead (which is open-source), but we'd have to start over on format, etc. Or perhaps we could use javaparser.org.

## Caveats ##

It's possible this tool can miss some dependencies. For example, pf-CDA determines dependencies from bytecode, and static constants are known to be inlined without any "backpointer" to the defining class.

In addition, the reflection-based references, being entered manually, are only as good as your team's ability to find them all.

But overall, this tool can probably get it 99% correct, which is close enough to start trying to actually move the decomposed code, at which point some gotchas will pop up that need to be dealt with.

Assume that your team can get within 99%, and then assume some iteration will be required to kill off the remaining illegal references.
