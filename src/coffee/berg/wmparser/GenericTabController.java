package coffee.berg.wmparser;

import coffee.berg.wmparser.DataModel.DataHolder;
import coffee.berg.wmparser.DataModel.DataPoint;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by Bergerking on 2018-11-29.
 */
public class GenericTabController
{
	private static final Logger logger = Logger.getLogger(GenericTabController.class.getName());

	@FXML
	private Button tstBtn;

	private StackPane listOfActions;
	private TableView<DataPoint> rollingLog;
	private StackedBarChart chart;

	private TreeMap tree;
	private TreeItem<String> rootNode;
	private TreeView<String> treeView;

	private DataHolder data;

	private Tab _thisTab;
	final static ExecutorService executor = Executors.newCachedThreadPool();

	Task<Void> sortingTask = new Task<Void>() {
		@Override protected Void call() throws Exception {
			while(!isCancelled())
			{
				try
				{
					CategoryAxis cat = (CategoryAxis) chart.getXAxis();
					cat.getCategories().sort((a,b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b)));
				} catch (Exception e)
				{
					if (Controller.testing)
						System.out.println(e.toString());
				}
				Thread.sleep(1000);
			}

			return null;
		}
	};

	public void setUp(final Tab _t, final DataHolder _dh)
	{
		data = _dh;
		_thisTab = _t;

		Node n = _thisTab.getContent();
		Node listofActions = n.lookup("#ListOfActions");
		Node rollingLog = n.lookup("#RollingLog");
		Node graph = n.lookup("#Graph");

		this.chart = (StackedBarChart) graph;
		this.rollingLog = (TableView<DataPoint>) rollingLog;
		this.listOfActions = (StackPane) listofActions;

		rootNode = new TreeItem<>("Actions");
		rootNode.setExpanded(true);
		treeView = new TreeView<>(rootNode);
		treeView.setId("TreeView");
		treeView.setEditable(true);
		treeView.setShowRoot(false);

		tree = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	}

	private boolean maybe = true;

	private int count = 0;

	@FXML
	public void testButton()
	{
//		Random random = new Random(System.currentTimeMillis());
//		chart.getData().remove(random.nextInt(chart.getData().size()-1));

//		ArrayList testList = new ArrayList();
//		testList.add(new DataNode(ConstantStrings.NATURAL_END_OF_ACTION, "boop"));
//		rollingLog.getItems().add(new DataPoint(LocalDate.now(), "test", "macroer", testList));

//		Random random = new Random(System.currentTimeMillis());
//
//		for (DataPoint dp : data.getDataPoints())
//		{
//			if(random.nextBoolean() || true)
//				dp.toggleVisible();
//		}

//		initializeBarChart(maybe);
//		maybe = !maybe;

//		Node n = _thisTab.getContent().lookup("#Graph");
//		Node n2 = n.getParent();
//		if (n2 instanceof VBox)
//		{
//			VBox vb = (VBox) n2;
//
//			StackedBarChart<String, Number> hold = initializeBarChart(false);
//			vb.getChildren().set(0, hold);
//
//			chart = hold;
//
//		}
//		else
//		{
//			System.out.println("Shit.");
//		}
	}

	@FXML
	public void testButton2()
	{
		CategoryAxis cat = (CategoryAxis) chart.getXAxis();
		cat.getCategories().sort((a,b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b)));
		return;
	}

	void handleClickedTreeleaf(CheckBoxTreeItem<String> _item)
	{
		logger.info("" + _item.getValue());
	}

	void initializeListOfActions ()
	{

		HashMap<String, Integer> hm = (HashMap<String, Integer>) data.getUniqueDataNodesAndCount(true, false);
		tree.putAll(hm);

		//TODO investigate making a more dynamic class that can easier represent number of visible.
		ArrayList<String> al = new ArrayList<>();
		tree.forEach((x, y) -> al.add(x + ": " + y));



		treeView.setCellFactory(CheckBoxTreeCell.<String>forTreeView());

		for(String s : al) {
			CheckBoxTreeItem<String> childNode = new CheckBoxTreeItem<>(s);
			childNode.setSelected(true);
			rootNode.getChildren().add(childNode);
		}

		listOfActions.getChildren().add(treeView);

	}

	void initializeRollingLog ()
	{

		HashMap<String, Integer> tempHash = (HashMap<String, Integer>) data.getUniqueDataNodesAndCount(false, true);
		TreeMap<String, Integer> tempMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		ArrayList<String> tempArrayList = new ArrayList<>();
		tempMap.putAll(tempHash);
		tempMap.forEach((x, y) -> tempArrayList.add(x + ": " + y));

		Node foundNode = listOfActions.lookup("#TreeView");

		if (foundNode == null)
		{
			logger.severe("Never update RollingLog before you have updated list of actions");
		}

		TreeView<String> treeView = (TreeView<String>) foundNode;
		TreeItem<String> rootNode = treeView.getRoot();

		ObservableList<TreeItem<String>> obsL = rootNode.getChildren().sorted();
		ArrayList<DataPoint> hold = data.getDataPoints();
		ArrayList<DataPoint> toDisplay = new ArrayList<>();

		for (DataPoint d : hold)
		{
			d.checkVisible();
			if (d.isVisible())
				toDisplay.add(d);

		}


		for(String s : tempArrayList) {
			boolean found = false;
			for(TreeItem<String> node : obsL) {

				String[] matchThis = node.getValue().split(": ");
				if(matchThis.length == 2)
				{

					if(s.contains(matchThis[0])) {
						CheckBoxTreeItem<String> newLeaf = new CheckBoxTreeItem<>(s);
						newLeaf.setSelected(true);
						newLeaf.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(),
								e -> handleClickedTreeleaf(newLeaf));
						node.getChildren().add(newLeaf);
						found = true;
					}
				}
				else System.out.println("not match this! "+ node.getValue());


			}
			if(!found && Controller.testing) System.out.println("Couldn't find container for: "+ s);

		}

		TableColumn<DataPoint, String> dateColumn = new TableColumn<>("Date");
		TableColumn<DataPoint, String> timeColumn = new TableColumn<DataPoint, String>("Time");
		TableColumn<DataPoint, String> dataColumn = new TableColumn<DataPoint, String>("Data");

		dateColumn.setCellValueFactory(new PropertyValueFactory<DataPoint, String>("date"));
		timeColumn.setCellValueFactory(new PropertyValueFactory<DataPoint, String>("timestamp"));
		dataColumn.setCellValueFactory(new PropertyValueFactory<DataPoint, String>("tokens"));

		ObservableList<DataPoint> obs = FXCollections.observableArrayList(toDisplay);

		rollingLog.setItems(obs);
		rollingLog.setEditable(true);
		rollingLog.getColumns().addAll(dateColumn, timeColumn, dataColumn);

	}

	XYChart.Series<String, Number> bullshit()
	{
		XYChart.Series<String, Number> rv = new XYChart.Series<>();
		rv.setName("this is shit");
		ArrayList<XYChart.Data<String, Number>> temp = new ArrayList<>();

		for (int i = 0; i < 1000; i++)
		{
			temp.add(new XYChart.Data<>("" + i, 0));
		}

		rv.setData(
				FXCollections.observableArrayList(temp)
		);

		return rv;
	}

	void initializeBarChart(final boolean _sameNumbersOnly)
	{
		Node n = _thisTab.getContent().lookup("#Graph");
		Node n2 = n.getParent();
		if (n2 instanceof VBox)
		{
			VBox vb = (VBox) n2;

			StackedBarChart<String, Number> hold = makeBarChart(false);
			vb.getChildren().set(0, hold);

			chart = hold;

			if (!sortingTask.isRunning())
			{
				executor.submit(sortingTask);
			}

			return;

		}
	}

	StackedBarChart<String, Number> makeBarChart(final boolean _sameNumbersOnly)
	{
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();
		StackedBarChart<String, Number> test = new StackedBarChart<String, Number>(xAxis, yAxis);




		//Barchart
		test.setTitle("Summary");
		test.getData().clear();
		test.layout();
		test.setLegendVisible(true);
		test.setCategoryGap(1);

//		XYChart.Series<String, Number> bs = bullshit();
//		test.getData().add(bs);

		if (_sameNumbersOnly)
		{
//			test.getData().add(data.calculateIntervalsBetweenActions("", false));
		}
		else
		{
			ArrayList<String> tempArr = data.getUniqueActionNumbers();

			for(String s : tempArr)
			{
				XYChart.Series<String, Number> currentSeries = new XYChart.Series<>();
				currentSeries.setData(
						FXCollections.observableArrayList(
								data.calculateIntervalsBetweenActions(s, true)
						)
				);
				currentSeries.setName(s);
				test.getData().add(currentSeries);
			}
		}
//        chart.getData().addAll(dock.getSeriesOfTimes());
		test.getXAxis().setAutoRanging(true);
		test.getYAxis().setAutoRanging(true);


		ObservableList<XYChart.Series<String, Number>> xys = test.getData();
		for(XYChart.Series<String,Number> series : xys)
		{
			ArrayList<XYChart.Data> removelist = new ArrayList<>();

			for(XYChart.Data<String,Number> data : series.getData())
			{
				if(data.getYValue().equals(0))
				{
//					removelist.add(data);
				}
			}

			series.getData().removeAll(removelist);
			series.getData().forEach(d -> {
				Tooltip tip = new Tooltip();
				tip.setText(series.getName() + ", " + d.getYValue() +
						(d.getYValue().intValue() > 1 ? " times" : " time"));
				Tooltip.install(d.getNode(), tip);
			});
		}


		return test;
	}
}
