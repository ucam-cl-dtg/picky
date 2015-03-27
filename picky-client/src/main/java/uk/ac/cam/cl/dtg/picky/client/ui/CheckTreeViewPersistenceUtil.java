package uk.ac.cam.cl.dtg.picky.client.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import org.controlsfx.control.CheckModel;
import org.controlsfx.control.CheckTreeView;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

public class CheckTreeViewPersistenceUtil {

	public static void main(String[] args) {
		List<String> list = new ArrayList<String>();
		list.add("a,2");
		list.add("b");

		System.out.println(list);
		list = list.stream().map(s -> s.replaceAll(",", "\\\\,")).collect(Collectors.toList());

		String string = Joiner.on(",").join(list);
		System.out.println(string);

		String[] parts = string.split("(?<!\\\\),");
		System.out.println(Arrays.asList(parts));
		List<String> list2 = Arrays.asList(parts).stream().map(s -> s.replaceAll("\\\\,", ",")).collect(Collectors.toList());
		System.out.println(list2);
	}

	public static void restore(CheckTreeView<String> tree, String settings) {
		List<String> items = Arrays.asList(settings.split("(?<!\\\\);"));

		for (String itemString : items) {
			List<String> path = Arrays.asList(itemString.split("(?<!\\\\)/"));
			TreeItem<String> parent = tree.getRoot();

			path = path.stream().map(s -> s.replaceAll("\\\\;", ";").replaceAll("\\\\/", "/")).collect(Collectors.toList());

			for (int i = 0; i < path.size(); i++) {
				parent = findItem(parent, path.get(i));
			}

			if (parent != null) tree.getCheckModel().check(parent);
		}
	}

	private static TreeItem<String> findItem(TreeItem<String> base, String value) {
		if (base == null) return null;

		ObservableList<TreeItem<String>> children = base.getChildren();

		for (TreeItem<String> child : children) {
			if (Objects.equal(child.getValue(), value)) return child;
		}

		return null;
	}

	public static String persist(CheckTreeView<String> tree) {
		CheckModel<TreeItem<String>> model = tree.getCheckModel();
		ObservableList<TreeItem<String>> checkedItems = model.getCheckedItems();

		List<String> nodes = new ArrayList<String>();

		for (TreeItem<String> item : checkedItems) {
			nodes.add(serialize(item));
		}

		return Joiner.on(';').join(nodes);
	}

	private static String serialize(TreeItem<String> item) {
		List<String> path = new ArrayList<String>();

		path.add(item.getValue());
		while (item.getParent() != null && item.getParent().getParent() != null) {
			path.add(0, item.getParent().getValue());
			item = item.getParent();
		}

		List<String> nodesEscaped = path.stream()
				.map(s -> s.replaceAll(";", "\\\\;"))
				.map(s -> s.replaceAll("/", "\\\\/"))
				.collect(Collectors.toList());

		return Joiner.on("/").join(nodesEscaped);
	}

}
