from flask import Flask,render_template,redirect,request, session, g
from flask_login import LoginManager, current_user, login_user, logout_user, login_required, UserMixin
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import types
from flask import json
from base64 import b64encode
from os import urandom
from sqlalchemy.ext.mutable import Mutable, MutableList
from AlbumManager import AlbumManager
import ast


app = Flask(__name__)

login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login' # the login view of your application
app.config['SECRET_KEY'] = "lkkajdghdadkglajkgah" # a secret key for your app TODO
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///../dbdir/test.db'
db = SQLAlchemy(app)
albumManager = AlbumManager()

@app.route("/")
def home():
	print(current_user)
	if not current_user.is_anonymous:
		return "hey " + current_user.username + ":<a href='/logout'>Logout</a>"
	else:
		return "home: <a href='/register'>Register</a><a href='/login'>Login</a>"

@app.route('/login', methods = ['POST', 'GET'])
def login():
	username = request.json.get('username', None)
	print(username)
	password = request.json.get('password', None)

	validate = validateArgs(username,password)
	if validate == 'Success':
		user = User.query.filter_by(username=username).first()
		token = user.generate_auth_token();
		login_user(user, remember=True)
		next = request.args.get('next')
		if current_user.is_authenticated:
			return "Success " + token
	return validate


@app.route('/logout', methods=['POST'])
def logout():
	user = getToken()
	if not user:
		return "AuthenticationRequired"
	logout_user()
	user.token = None
	db.session.commit()
	return "Success"

@app.route('/register', methods=['POST'])
def register():
	username = request.json.get('username', None)
	print(username)
	password = request.json.get('password', None)
	users = User.query.filter_by(username=username) #TODO
	if users.count() != 0:
		return "UsernameTaken"
	else:
		u = User(username=username, password=password)
		db.session.add(u)
		db.session.commit()
		return "Success"

@app.route('/createAlbum', methods = ['POST'])
def createAlbum():
	user = getToken()
	if not user:
		return "AuthenticationRequired"
	print(user)
	print("----")
	print(User.query.filter_by(username=user.username).first().albums)
	albumName = request.json.get('albumName', None)
	albumUrl = request.json.get('albumUrl', None)
	userAlbums = albumManager.createAlbum(user, albumName, albumUrl)
	user = User.query.filter_by(username=user.username).first()
	user.albums = None
	db.session.commit()
	user = User.query.filter_by(username=user.username).first()
	user.albums = userAlbums
	print(user.albums)
	db.session.commit()
	print("----")
	print(User.query.filter_by(username=user.username).first().albums)
	return "Success"

@app.route('/getUsers', methods=['GET'])
def getUsers():
	user = getToken()
	if not user:
		return "AuthenticationRequired"
	print(user)
	users = User.query.all()
	usernames = []
	for u in users:
		if u.username != user.username:
			usernames += [u.username]
	res = ";".join(usernames)
	return res +";"

@app.route('/addUsersToAlbum', methods=['POST'])
def addUsersToAlbum():
	user = getToken()
	if not user:
		return "AuthenticationRequired"
	import random
	albumName = request.json.get('albumName', None)
	print(albumName)
	usernames = request.json.get('usernames', None)
	print(usernames)
	usernames = ast.literal_eval(usernames)
	for u in usernames:
		bdUser = User.query.filter_by(username=u).first()
		print("processing " + u)
		print(bdUser)
		if not bdUser.pendingInvites:
			print("No pending invites for " + bdUser.username)
			data = {user.username : [albumName]}
			print(data)
			bdUser.pendingInvites = data
			db.session.commit()
		else:
			print("Already pending invites for " + bdUser.username)
			data = bdUser.pendingInvites
			if (user.username in data):
				data[user.username] += [albumName]
			else:
				data[user.username] = [albumName]
			bdUser.pendingInvites = None
			db.session.commit()
			bdUser.pendingInvites = data
			db.session.commit()
			
	userAlbums = albumManager.addUsersToAlbum(user, albumName, usernames)
	print(userAlbums)
	if (userAlbums):
		user.albums = None
		db.session.commit()
		user = User.query.filter_by(username=user.username).first()
		user.albums = userAlbums
		print(user.albums)
		db.session.commit()
		print("SUCCESS")
		return "Success"
	print("ERROR")
	return "Error"

@app.route('/askForInvite', methods=['GET'])
def askforInvite():
	user = getToken()
	if not user:
		return "AuthenticationRequired"
	print(user)
	print(user.pendingInvites)
	if (user.pendingInvites):
		pendingInvites = user.pendingInvites
		userReq = list(pendingInvites.keys())[0]
		res = userReq + ";" +  str(pendingInvites[userReq][0])
		pendingInvites[userReq].remove(pendingInvites[userReq][0])
		print(res)
		print(pendingInvites)
		if (len(pendingInvites[userReq]) == 0):
			pendingInvites.pop(userReq)
		print(pendingInvites)
		#TODO this should not be here
		user.pendingInvites = None
		db.session.commit()
		user.pendingInvites = pendingInvites
		db.session.commit()
		return res
	return "Empty"

@app.route('/acceptInvitation', methods=['POST'])
def acceptInvitation():
	user = getToken()
	if not user:
		return "AuthenticationRequired"
	sliceUrl = request.json.get('dropboxUrl', None)
	print(sliceUrl)
	creator = request.json.get('userAlbum', None)
	print(creator)
	albumName = request.json.get('albumName', None)
	print(albumName)
	success = request.json.get('success', None) #TODO
	print(success)
	creatorUser = User.query.filter_by(username=creator).first()
	userAlbum = albumManager.addUsersToAlbum(creatorUser, albumName, [user.username], sliceUrl)
	if (userAlbum):
		user.albums = None
		db.session.commit()
		user = User.query.filter_by(username=user.username).first()
		user.albums = userAlbum
		db.session.commit()
		return "Success"
	return "Error"

def getToken():
	token = request.headers.get('authorization')
	print(token)
	if not token:
		print("No Token Received")
		return False
	user = verify_token(token)
	if not user:
		print("Invalid Token")
		return False
	return user

def verify_token(token):
	user = User.query.filter_by(token=token).first()
	if not user:
		return False
	return user

#This callback reloads the user from the ID stored in the session
@login_manager.user_loader
def get_user(user_id):
	print("here")
	u = User.query.get(user_id).first()
	return User.query.get(int(user_id))

class User(UserMixin, db.Model):
	id = db.Column(db.Integer, primary_key=True)
	username = db.Column(db.String(80), unique=True, nullable=False)
	password = db.Column(db.String(50), unique=False, nullable = False)
	token = db.Column(db.String(150))
	albums = db.Column(types.PickleType)
	pendingInvites = db.Column(types.PickleType)

	def __repr__(self):
		return '<User %r %r>' % (self.id, self.username)

	def generate_auth_token(self, expiration = 600):
		t_bytes = urandom(64)
		token = b64encode(t_bytes).decode('utf-8')
		self.token = token
		db.session.commit()
		return token


def validateArgs(username, password):
	print(User.query.filter_by(username=username).count())
	print(User.query.filter_by(username=username).first())
	if User.query.filter_by(username=username).count() == 0:
		return 'UnknownUser'
	user = User.query.filter_by(username=username).first()
	if user.password != password:
		return 'IncorrectPassword'
	return 'Success'
