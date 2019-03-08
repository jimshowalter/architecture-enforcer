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

package com.jimandlisa.enforcer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

public class TargetUtils {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	static String yaml(File file) throws Exception {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(LINE_SEPARATOR);
			}
		}
		return builder.toString();
	}
	
	static JSONObject json(String yaml) {
		Map<String, Object> map = new Yaml().load(yaml);
		return new JSONObject(map);
	}
	
	@SuppressWarnings("unchecked") // Weak.
	static Map<String, Object> cast(Object obj) {
		return (Map<String, Object>)obj;
	}
	
	private static final Set<String> ALLOWED_LAYER_KEYS = new HashSet<>(Arrays.asList(new String[] {"name", "depth", "description"}));
	private static final Set<String> ALLOWED_DOMAIN_KEYS = new HashSet<>(Arrays.asList(new String[] {"name", "description"}));
	private static final Set<String> ALLOWED_COMPONENT_KEYS = new HashSet<>(Arrays.asList(new String[] {"name", "layer", "domain", "description", "packages", "classes"}));
	
	static String kind(Set<String> allowedKeys) {
		if (allowedKeys == ALLOWED_LAYER_KEYS) {
			return "layer";
		}
		if (allowedKeys == ALLOWED_DOMAIN_KEYS) {
			return "domain";
		}
		return "component";
	}
	
	static Errors error(Set<String> allowedKeys) {
		if (allowedKeys == ALLOWED_LAYER_KEYS) {
			return Errors.UNRECOGNIZED_LAYER_KEY;
		}
		if (allowedKeys == ALLOWED_DOMAIN_KEYS) {
			return Errors.UNRECOGNIZED_DOMAIN_KEY;
		}
		return Errors.UNRECOGNIZED_COMPONENT_KEY;
	}
	
	static void validate(Map<String, Object> map, Set<String> allowed) {
		Set<String> set = new HashSet<>(map.keySet());
		set.removeAll(allowed);
		if (!set.isEmpty()) {
			if (set.size() == 1) {
				throw new EnforcerException("unrecognized " + kind(allowed) + " key: " + set.iterator().next(), error(allowed));
			}
			StringBuilder builder = new StringBuilder();
			boolean first = true;
			for (String key : CollectionUtils.sort(new ArrayList<>(set))) {
				if (first) {
					first = false;
				} else {
					builder.append(", ");
				}
				builder.append(key);
			}
			throw new EnforcerException("unrecognized " + kind(allowed) + " keys: " + builder.toString(), error(allowed));
		}
	}
	
	static String get(Map<String, Object> map, String key) {
		return (String)map.get(key);
	}
	
	static Integer getInteger(Map<String, Object> map, String key) {
		return (Integer)map.get(key);
	}
	
	@SuppressWarnings("unchecked") // Weak.
	static List<String> getList(Map<String, Object> map, String key) {
		return (List<String>)map.get(key);
	}
	
	static Layer layer(Map<String, Layer> layers, String name) {
		return layers.get(ArgUtils.check(name, "layer name"));
	}
	
	static Domain domain(Map<String, Domain> domains, String name) {
		return domains.get(ArgUtils.check(name, "domain name"));
	}

	public static Target parse(File file) throws Exception {
		JSONObject json = json(yaml(file));
		Target target = new Target();
		Set<Integer> depths = new HashSet<>();
		for (Object obj : json.getJSONArray("layers").toList()) {
			Map<String, Object> map = cast(obj);
			validate(map, ALLOWED_LAYER_KEYS);
			Layer layer = new Layer(get(map, "name"), getInteger(map, "depth"), get(map, "description"));
			if (depths.contains(layer.depth())) {
				throw new EnforcerException("duplicate layer depth " + layer.depth(), Errors.DUPLICATE_LAYER_DEPTH);
			}
			depths.add(layer.depth());
			if (target.layers().containsKey(layer.name())) {
				throw new EnforcerException("duplicate layer name " + layer.quotedName(), Errors.DUPLICATE_LAYER_NAME);
			}
			target.add(layer);
		}
		if (json.has("domains")) {
			for (Object obj : json.getJSONArray("domains").toList()) {
				Map<String, Object> map = cast(obj);
				validate(map, ALLOWED_DOMAIN_KEYS);
				Domain domain = new Domain(get(map, "name"), get(map, "description"));
				if (target.domains().containsKey(domain.name())) {
					throw new EnforcerException("duplicate domain name " + domain.quotedName(), Errors.DUPLICATE_DOMAIN_NAME);
				}
				target.add(domain);
			}
		}
		Map<String, Component> allPackages = new HashMap<>();
		Map<String, Component> allClasses = new HashMap<>();
		boolean requireDomains = !target.domains().isEmpty();
		for (Object obj : json.getJSONArray("components").toList()) {
			Map<String, Object> map = cast(obj);
			validate(map, ALLOWED_COMPONENT_KEYS);
			Component component = new Component(get(map, "name"), layer(target.layers(), get(map, "layer")), (requireDomains ? domain(target.domains(), get(map, "domain")) : null), get(map, "description"));
			if (target.components().containsKey(component.name())) {
				throw new EnforcerException("duplicate component name " + component.quotedName(), Errors.DUPLICATE_COMPONENT_NAME);
			}
			component.layer().components().put(component.name(), component);
			if (component.domain() != null) {
				component.domain().components().put(component.name(), component);
			}
			List<String> packages = getList(map, "packages");
			if (packages != null) {
				for (String pkg : packages) {
					String normalized = pkg.replaceAll("[.]+$", "");
					Component other = allPackages.get(normalized);
					if (other != null) {
						throw new EnforcerException("duplicate package name used in " + other.quotedName() + " and " + component.quotedName(), Errors.DUPLICATE_PACKAGE_NAME);
					}
					component.packages().add(normalized);
					allPackages.put(normalized, component);
				}
			}
			List<String> classes = getList(map, "classes");
			if (classes != null) {
				for (String clazz : classes) {
					String normalized = clazz.trim();
					Component other = allClasses.get(normalized);
					if (other != null) {
						throw new EnforcerException("duplicate class name used in " + other.quotedName() + " and " + component.quotedName(), Errors.DUPLICATE_CLASS_NAME);
					}
					component.classes().add(normalized);
					allClasses.put(normalized, component);
				}
			}
			target.add(component);
		}
		return target;
	}
	
	public static void dump(Target target, PrintStream console) throws Exception {
		List<Layer> layers = new ArrayList<Layer>(target.layers().values());
		Collections.sort(layers, new Comparator<Layer>(){
			@Override
			public int compare(Layer l1, Layer l2) {
				return Integer.valueOf(l1.depth()).compareTo(Integer.valueOf(l2.depth()));
			}});
		List<Domain> domains = new ArrayList<Domain>(target.domains().values());
		Collections.sort(domains, new Comparator<Domain>(){
			@Override
			public int compare(Domain d1, Domain d2) {
				return d1.name().compareTo(d2.name());
			}});
		List<Component> components = new ArrayList<Component>(target.components().values());
		Collections.sort(components, new Comparator<Component>(){
			@Override
			public int compare(Component d1, Component d2) {
				return d1.name().compareTo(d2.name());
			}});
		console.println("Target-state specification:");
		console.println("\tLAYERS:");
		for (Layer layer : layers) {
			console.println("\t\t" + layer);
			console.println("\t\t\t" + layer.description());
		}
		console.println("\tDOMAINS:");
		for (Domain domain : domains) {
			console.println("\t\t" + domain);
			console.println("\t\t\t" + domain.description());
		}
		console.println("\tCOMPONENTS:");
		for (Component component : components) {
			console.println("\t\t" + component);
			console.println("\t\t\t" + component.description());
			console.println("\t\t\tpackages:");
			for (String pkg : component.packages()) {
				console.println("\t\t\t\t" + pkg);
			}
			console.println("\t\t\tclasses:");
			for (String clazz : component.classes()) {
				console.println("\t\t\t\t" + clazz);
			}
		}
	}
}
