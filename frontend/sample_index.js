// Line Chart initialisation and styling
const ctx = document.getElementById("line-chart").getContext("2d");
const chart = new Chart(ctx, {
  type: "line",
  data: {
    labels: [],
    datasets: [
      {
        label: "Real-Time Data",
        data: [], // Empty array initialisation prior to hardware connection
        borderColor: "rgba(75, 192, 192, 1)",
        borderWidth: 2,
        fill: false,
      },
    ],
  },
  options: {
    scales: {
      x: {
        type: "linear",
        position: "bottom",
      },
      y: {
        beginAtZero: true,
      },
    },
  },
});

// Function to update the chart with new data
function updateChart(newValue) {
  // Add the new data point
  chart.data.labels.push(chart.data.labels.length);
  chart.data.datasets[0].data.push(newValue);

  // Constant to limit sliding data window to 20 data points
  const maxDataPoints = 20;
  if (chart.data.datasets[0].data.length > maxDataPoints) {
    chart.data.datasets[0].data.shift();
  }

  // Update the chart
  chart.update();
}

// Create a SockJS connection
const socket = new SockJS("<my ngrok tunnel>");

// Create a STOMP client over the SockJS connection
const stompClient = Stomp.over(socket);

// Subscribe to WS topic
stompClient.connect({}, () => {
  console.log("Connected to SockJS and STOMP");
  stompClient.subscribe("/<topic name>/<>", (message) => {
    try {
      const data = JSON.parse(message.body);
      const newValue = data.value;
      updateChart(newValue);
    } catch (error) {
      console.error("Error parsing STOMP message:", error);
    }
  });
});
