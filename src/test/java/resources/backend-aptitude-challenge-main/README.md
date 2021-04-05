# Swat-Intel Backend Aptitude Challenge
Welcome to the SWAT-Intel Aptitude Challenge. You can develop your work as follows:

**Mission:**
Create a supply transport route search REST API

**Estimated time**
4 Hours

**Premises:**
- The REST API should be written in Java/Kotlin with Spring or C# with .NET Core.
- Database is not necessary.

**What will be evalueted:**
- Code and solution's quality

**The resources (on this repository) you should use:**
- There is a [CSV from iTrain](iTrain.csv) and a [JSON from UberOnRails](uberOnRails.json), each one with a list of train trips scheduled by the company between 2021/02/10 and 2021/02/18.
- A [JSON of the train stations](trainStations.json) available.
- They are structured as follows:

**iTrain**
| Field | Format
|--|--|
| trip_number | Alphanumeric (6 char length)
| origin_station | Alphanumeric (3 char length)
| destiny_station | Alphanumeric (3 char length)
| date | DATE
| departure_time | TIME
| arrival_time | TIME
| price | decimal

**UberOnRails**
| Field | Format
|--|--|
| trip | Alphanumeric (8 char length)
| origin | Alphanumeric (3 char length)
| destiny | Alphanumeric (3 char length)
| departureDate | DATE
| departure | TIME
| arrival | TIME
| value | decimal

**TrainStations**
| Field | Format
|--|--|
| name | Alphanumeric
| station | Alphanumeric (3 char length)
| city | Alphanumeric

**Objective A:**
- Provide 1 (one) endpoint that receives `Origin station`, `Destiny station` and optionally `Trip date` criterias and return a JSON with all the available routes (composed by one or more trips) of the two companies (iTrain and UberOnRails) matching the search criteria, ordened by date and arrival time.
- Trip connections can be used as long as the interval between the trips are equal or less than 12 hours.
- A trip from iTrain can connect with a trip from UberOnRails, there is no problem with that.
- The max of trip connections is up to you.

**Objective B:**
- Provide 1 (one) endpoint that returns the list of all the train stations that completely or partialy match a `Station name` criteria.
- The criteria should not be required to use the endpoint.
- The criteira matching should not be case sensitive.

**Response example:**

```json
[
	{
		"origin": "GRU",
		"destiny": "LOA",
		"departure": "YYYY-MM-DDTHH:mm:ss.sssZ",
		"arrival": "YYYY-MM-DDTHH:mm:ss.sssZ",
		"steps": [
			{
				"origin": "GRU",
				"destiny": "NYC",
				"departure": "YYYY-MM-DDTHH:mm:ss.sssZ",
				"arrival": "YYYY-MM-DDTHH:mm:ss.sssZ",
				"company": "UberOnRails",
				"price": 1400.00
			},
			{
				"origin": "NYC",
				"destiny": "LOA",
				"departure": "YYYY-MM-DDTHH:mm:ss.sssZ",
				"arrival": "YYYY-MM-DDTHH:mm:ss.sssZ",
				"company": "UberOnRails",
				"price": 350.00
			}
		]
	}
]
```

**Extras:**
Add all the dependencies or resources you think that is necessary to develop the best solution for the challenge. Some examples, but you don't need to limitate yourself on it:

- Documentation (Swagger, OpenAPI etc)
- Cache
- Docker
- Tests

**Upload the solution:**
- Your solution should be uploaded on a Github or Bitbucket public repository.
The repository must have:
- The project.
- A README explaining how to run your project.