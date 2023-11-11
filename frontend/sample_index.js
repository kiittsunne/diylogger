// Line Chart initialisation and styling
const ctx = document.getElementById("line-chart").getContext("2d");
const chart = new Chart(ctx, {
  type: "line",
  data: {
    labels: [],
    datasets: [],
  },
  options: {
    adapters: {
      date: {
        locale: "en_SG",
      },
    },
    scales: {
      x: {
        ticks: {
          // For a category axis, the val is the index so the lookup via getLabelForValue is needed
          callback: function (val) {
            return this.getLabelForValue(val);
          },
          color: "grey",
          maxRotation: 50,
        },
      },
      y: {
        beginAtZero: true,
        min: 8,
        suggestedMax: 14,
      },
    },
  },
});

let dateTimeStamp = "";

// Function to update the chart with new data
function updateChart(sensorId, newValue, timeStamp) {
  const maxDataPoints = 20;

  // Find the dataset for the specified sensorId
  const datasetIndex = chart.data.datasets.findIndex(
    (dataset) => dataset.label === sensorId
  );

  if (datasetIndex === -1) {
    // If the dataset for the sensorId doesn't exist, create a new one
    const newDatasetTemplate = {
      label: sensorId,
      data: [newValue],
      borderColor: "rgba(75, 192, 192, 1)",
      borderWidth: 1,
      fill: false,
    };

    if (sensorId === "average") {
      newDatasetTemplate.borderColor = "rgba(192, 141, 75, 1)";
      newDatasetTemplate.fill = true;
    }

    chart.data.datasets.push(newDatasetTemplate);
  } else {
    // Add the new data point
    chart.data.datasets[datasetIndex].data.push(newValue);

    // Constant to limit sliding data window to 20 data points
    if (chart.data.datasets[datasetIndex].data.length > maxDataPoints) {
      chart.data.datasets[datasetIndex].data.shift();
    }
  }

  // Add the current timestamp to the labels
  if (dateTimeStamp != timeStamp) {
    dateTimeStamp = timeStamp;
    chart.data.labels.push(timeStamp);
  }

  // Remove labels if they exceed the maximum number of data points
  if (chart.data.labels.length > maxDataPoints) {
    chart.data.labels.shift();
  }

  // Update the chart
  chart.update();
}

// Create a SockJS connection
const socket = new SockJS("https://my.ngrok.endpoint");

// Create a STOMP client over the SockJS connection
const stompClient = Stomp.over(socket);

// Subscribe to WS topic
stompClient.connect({}, () => {
  console.log("Connected to SockJS and STOMP");
  stompClient.subscribe("/my/topic", (message) => {
    try {
      const data = JSON.parse(message.body);
      const sensorId = data.sensorId;
      const newValue = data.value;
      const timeStamp = data.timeStamp;
      updateChart(sensorId, newValue, timeStamp);
    } catch (error) {
      console.error("Error parsing STOMP message:", error);
    }
  });
});
