<?php

// verify if the group exists in order to let

$servername = "127.0.0.1";
$username = "root";
$password = "";
$dbname = "my_watlocate";

$gruppo = $_POST["groupID"];

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
$sql = "SELECT * FROM `positions` WHERE groupID='$gruppo'";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
	$response["success"] = 1;
    $response["message"] = "Logged to group \"$gruppo\"";
    $response["numrows"] = $result->num_rows;
    $response["gruppo"] = $gruppo;
    echo json_encode($response);
    
} else {
	$response["success"] = 0;
    $response["message"] = "Group \"$gruppo\" doesn't exists.\n Please enter AGAIN";
    echo json_encode($response);
}

$conn->close();
?> 