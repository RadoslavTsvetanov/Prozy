package ConfigParsing

"""
{
  "logging": NONE,
  "port": "8080",
  "host": "0.0.0.0",
  "cors": {
    "allowedOrigins": [
      "*"
    ]
  }
}  
"""



def test1() = {
  expectedConfig = new Config(middlewares = [], redirection_handlers = [], logging = None, ssl = ,port = "8080", host = "0.0.0.0", cors = )
}