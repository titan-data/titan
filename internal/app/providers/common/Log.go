package common

import (
	"fmt"
	"strconv"
)

func Log(repo string, tags []string, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	first := true
	//opts := client.ListCommitsOpts{Tag:tags}
	commits, _, _ := commitsApi.ListCommits(ctx, repo, nil)

	for _, commit := range commits {
		if !first {
			fmt.Println("")
		} else {
			first = false
		}
		metadata := commit.Properties
		fmt.Println("commit " + commit.Id)
		ifContainsPrint(metadata, "author")
		ifContainsPrint(metadata, "user")
		ifContainsPrint(metadata, "email")
		ifContainsPrint(metadata, "timestamp")

		//TODO tags

		if metadata["message"] != "" {
			out := fmt.Sprintf("\n%v", metadata["message"])
			fmt.Println(out)
		}
	}





	//private val n = System.lineSeparator()
	//
	//fun log(container: String, tags: List<String>) {
	//	var first = true
	//	for (commit in commitsApi.listCommits(container, tags)) {
	//		if (!first) {
	//			println("")
	//		} else {
	//			first = false
	//		}
	//		val metadata = commit.properties
	//		if (metadata.containsKey("tags")) {
	//			@Suppress("UNCHECKED_CAST")
	//			val tags = metadata.get("tags") as Map<String, String>
	//			if (!tags.isEmpty()) {
	//				print("Tags:")
	//				for ((key, value) in tags) {
	//					print(" ")
	//					if (value != "") {
	//						print("$key=$value")
	//					} else {
	//						print(key)
	//					}
	//				}
	//				println("")
	//			}
	//		}
	//	}
	//}
}


