from flask import Flask, redirect, url_for, request
app = Flask(__name__)

@app.route("/")
def ():
    return "Hello World!"

@app.route("/register", methods=['POST'])
def register(username, password):
	username = request.get_json.get('username', None)
	password = request.get_json.get('password', None)

@app.route("/login", methods=['POST'])
def login(username, password):

@app.route("/logout", methods=['POST'])
def logout(username, password):

if __name__ == "__main__":
	app.run(host='0.0.0.0', port=8350)