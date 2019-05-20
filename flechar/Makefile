test:
	npm install
	npx shadow-cljs compile ci-tests
	npx karma start --single-run
	clj -A:dev:clj-tests

dev:
	npm install
	npx shadow-cljs server

browse:
	clj -A:dev -J-Dtrace

.PHONY: test dev
